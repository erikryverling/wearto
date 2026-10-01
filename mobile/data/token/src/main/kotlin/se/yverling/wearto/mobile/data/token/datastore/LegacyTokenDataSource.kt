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

// TODO: Temporary compatibility code for pre-AEAD upgrades. Remove in a separate release after the supported upgrade window closes.
internal interface LegacyTokenDataSource {
    val tokenFlow: Flow<String?>
    suspend fun clearToken()
}

// TODO: Temporary compatibility code for pre-AEAD upgrades. Remove in a separate release after the supported upgrade window closes.
internal class LegacyTokenDataSourceImpl(
    private val dataStore: DataStore<String?>,
) : LegacyTokenDataSource {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        serializer: TokenDataStoreSerializer,
    ) : this(
        dataStore = DataStoreFactory.create(
            serializer = serializer,
            produceFile = { context.preferencesDataStoreFile(DATASTORE_FILE_NAME) },
        )
    )

    override val tokenFlow: Flow<String?> = dataStore.data

    override suspend fun clearToken() {
        withContext(Dispatchers.IO) {
            dataStore.updateData { null }
        }
    }

    companion object {
        private const val DATASTORE_FILE_NAME = "token"
    }
}
