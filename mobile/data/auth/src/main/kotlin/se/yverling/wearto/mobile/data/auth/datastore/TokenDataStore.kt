package se.yverling.wearto.mobile.data.auth.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal const val DATASTORE_FILE_NAME = "token"

internal class TokenDataSource @Inject constructor(
    @ApplicationContext context: Context,
    serializer: TokenDataStoreSerializer,
) {
    private val dataStore: DataStore<String?> = DataStoreFactory.create(
        serializer = serializer,
        produceFile = { context.preferencesDataStoreFile(DATASTORE_FILE_NAME) }
    )

    // TODO Can we avoid nullable?
    val tokenFlow: Flow<String?> = dataStore.data

    suspend fun persistToken(account: String) {
        withContext(Dispatchers.IO) {
            dataStore.updateData { account }
        }
    }

    suspend fun clearToken() {
        withContext(Dispatchers.IO) {
            dataStore.updateData { null }
        }
    }
}
