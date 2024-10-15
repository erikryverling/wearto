package se.yverling.wearto.mobile.data.token

import kotlinx.coroutines.flow.Flow

interface TokenRepository {
    suspend fun setToken(token: String)
    fun getToken(): Flow<String?>
    fun hasToken(): Flow<Boolean>
    suspend fun clearToken()
}
