package se.yverling.wearto.mobile.data.token.datastore

import kotlinx.coroutines.flow.Flow

internal interface TokenDataSource {
    val tokenFlow: Flow<String?>
    suspend fun persistToken(token: String)
    suspend fun clearToken()
}
