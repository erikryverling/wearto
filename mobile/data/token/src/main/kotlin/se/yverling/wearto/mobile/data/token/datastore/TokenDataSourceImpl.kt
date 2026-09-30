package se.yverling.wearto.mobile.data.token.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class TokenDataSourceImpl @Inject constructor(
    @ApplicationContext context: Context,
    serializer: TokenDataStoreSerializer,
) : TokenDataSource {
    private val dataStore: DataStore<String?> = DataStoreFactory.create(
        serializer = serializer,
        produceFile = { context.preferencesDataStoreFile(DATASTORE_FILE_NAME) },
    )

    override val tokenFlow: Flow<String?> = dataStore.data

    override suspend fun persistToken(token: String) {
        withContext(Dispatchers.IO) {
            dataStore.updateData { token }
        }
    }

    override suspend fun clearToken() {
        withContext(Dispatchers.IO) {
            dataStore.updateData { null }
        }
    }

    companion object {
        private const val DATASTORE_FILE_NAME = "token"
    }
}
