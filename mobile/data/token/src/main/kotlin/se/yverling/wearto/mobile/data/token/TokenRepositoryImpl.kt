package se.yverling.wearto.mobile.data.token

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.InternalSerializationApi
import se.yverling.wearto.mobile.data.token.datastore.LegacyTokenDataSource
import se.yverling.wearto.mobile.data.token.datastore.StoredTokenState
import se.yverling.wearto.mobile.data.token.datastore.TokenDataSource
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, InternalSerializationApi::class)
internal class TokenRepositoryImpl @Inject constructor(
    private val tokenDataSource: TokenDataSource,
    private val legacyTokenDataSource: LegacyTokenDataSource,
) : TokenRepository {

    override fun getToken(): Flow<String?> {
        return tokenDataSource.stateFlow.flatMapLatest { aeadState ->
            when (aeadState) {
                is StoredTokenState.Present -> flowOf(aeadState.token)
                StoredTokenState.Absent -> flowOf(null)
                StoredTokenState.Unset -> legacyTokenDataSource.tokenFlow
            }
        }
    }

    override suspend fun setToken(token: String) {
        tokenDataSource.persistToken(token)
    }

    override suspend fun clearToken() {
        tokenDataSource.clearToken()
        try {
            legacyTokenDataSource.clearToken()
        } catch (e: Exception) {
            Timber.d(e, "Clearing legacy token failed")
        }
    }

    override fun hasToken(): Flow<Boolean> = getToken().map { it != null }
}
