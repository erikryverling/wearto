package se.yverling.wearto.mobile.data.token

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.yverling.wearto.mobile.data.token.datastore.TokenDataSource
import javax.inject.Inject

internal class TokenRepositoryImpl @Inject constructor(
    private val dataStore: TokenDataSource,
) : TokenRepository {
    override fun getToken() = dataStore.tokenFlow

    override suspend fun setToken(token: String) {
        dataStore.persistToken(token)
    }

    override suspend fun clearToken() {
        dataStore.clearToken()
    }

    override fun hasToken(): Flow<Boolean> = getToken().map { it != null }
}
