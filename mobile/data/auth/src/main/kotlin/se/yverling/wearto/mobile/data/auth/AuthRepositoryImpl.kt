package se.yverling.wearto.mobile.data.auth

import se.yverling.wearto.mobile.data.auth.datastore.TokenDataSource
import javax.inject.Inject

internal class AuthRepositoryImpl @Inject constructor(
    private val dataStore: TokenDataSource,
) : AuthRepository {
    override suspend fun login(token: String) {
        dataStore.persistToken(token)
    }

    override suspend fun logout() {
        dataStore.clearToken()
    }

    override fun getToken() = dataStore.tokenFlow
}
