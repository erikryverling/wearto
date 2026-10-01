package se.yverling.wearto.mobile.data.token

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.InternalSerializationApi
import se.yverling.wearto.mobile.data.token.datastore.LegacyTokenDataSource
import se.yverling.wearto.mobile.data.token.datastore.StoredTokenState
import se.yverling.wearto.mobile.data.token.datastore.TokenDataSource
import timber.log.Timber
import java.io.IOException
import java.security.GeneralSecurityException
import javax.inject.Inject

@OptIn(InternalSerializationApi::class)
internal class TokenRepositoryImpl @Inject constructor(
    private val tokenDataSource: TokenDataSource,
    // TODO: Temporary compatibility code for pre-AEAD upgrades. Remove in a separate release after the supported upgrade window closes.
    private val legacyTokenDataSource: LegacyTokenDataSource,
) : TokenRepository {

    private val migrationMutex = Mutex()
    private var migrationAttempted = false

    override fun getToken(): Flow<String?> = flow {
        migrateIfNeeded()
        emitAll(
            tokenDataSource.stateFlow.map { aeadState ->
                when (aeadState) {
                    is StoredTokenState.Present -> aeadState.token
                    is StoredTokenState.Absent, is StoredTokenState.Unset -> null
                }
            }
        )
    }.catch { throwable ->
        when (throwable) {
            is UnrecoverableTokenException -> throw throwable
            is CorruptionException, is GeneralSecurityException -> {
                throw UnrecoverableTokenException("Stored credential could not be decrypted or read", throwable)
            }
            is IOException -> throw throwable
            else -> throw throwable
        }
    }

    override suspend fun setToken(token: String) {
        migrationMutex.withLock {
            tokenDataSource.persistToken(token)
            try {
                legacyTokenDataSource.retireLegacyData()
            } catch (e: Exception) {
                Timber.d(e, "Retiring legacy data on setToken failed")
            }
            migrationAttempted = true
        }
    }

    override suspend fun clearToken() {
        migrationMutex.withLock {
            tokenDataSource.clearToken()
            try {
                legacyTokenDataSource.retireLegacyData()
            } catch (e: Exception) {
                Timber.d(e, "Retiring legacy data on clearToken failed")
            }
            migrationAttempted = true
        }
    }

    override fun hasToken(): Flow<Boolean> = getToken().map { it != null }

    // TODO: Temporary compatibility code for pre-AEAD upgrades. Remove in a separate release after the supported upgrade window closes.
    private suspend fun migrateIfNeeded() {
        if (migrationAttempted) return
        migrationMutex.withLock {
            if (migrationAttempted) return
            val currentAeadState = try {
                tokenDataSource.stateFlow.first()
            } catch (e: Exception) {
                when (e) {
                    is CorruptionException, is GeneralSecurityException -> {
                        throw UnrecoverableTokenException("Failed to read AEAD token store", e)
                    }
                    else -> throw e
                }
            }
            if (currentAeadState is StoredTokenState.Unset) {
                val legacyToken = try {
                    legacyTokenDataSource.getLegacyToken()
                } catch (e: Exception) {
                    when (e) {
                        is CorruptionException, is GeneralSecurityException -> {
                            throw UnrecoverableTokenException("Failed to read legacy token store", e)
                        }
                        else -> throw e
                    }
                }
                if (!legacyToken.isNullOrBlank()) {
                    tokenDataSource.persistToken(legacyToken)
                    legacyTokenDataSource.retireLegacyData()
                }
            }
            migrationAttempted = true
        }
    }
}
