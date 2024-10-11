package se.yverling.wearto.mobile.data.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(token: String)
    suspend fun logout()
    fun getToken(): Flow<String?>
}
