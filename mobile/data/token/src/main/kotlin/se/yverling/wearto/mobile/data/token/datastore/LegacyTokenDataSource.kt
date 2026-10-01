package se.yverling.wearto.mobile.data.token.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

// TODO: Temporary compatibility code for pre-AEAD upgrades. Remove in a separate release after the supported upgrade window closes.
internal interface LegacyTokenDataSource {
    val tokenFlow: Flow<String?>
    suspend fun getLegacyToken(): String?
    suspend fun clearToken()
    suspend fun retireLegacyData()
}

// TODO: Temporary compatibility code for pre-AEAD upgrades. Remove in a separate release after the supported upgrade window closes.
internal class LegacyTokenDataSourceImpl(
    private val dataStore: DataStore<String?>,
    private val produceFile: () -> File = { File("") },
) : LegacyTokenDataSource {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        serializer: TokenDataStoreSerializer,
    ) : this(
        dataStore = DataStoreFactory.create(
            serializer = serializer,
            produceFile = { context.preferencesDataStoreFile(DATASTORE_FILE_NAME) },
        ),
        produceFile = { context.preferencesDataStoreFile(DATASTORE_FILE_NAME) },
    )

    override val tokenFlow: Flow<String?> = dataStore.data

    override suspend fun getLegacyToken(): String? = dataStore.data.first()

    override suspend fun clearToken() {
        withContext(Dispatchers.IO) {
            dataStore.updateData { null }
        }
    }

    override suspend fun retireLegacyData() {
        withContext(Dispatchers.IO) {
            try {
                dataStore.updateData { null }
            } catch (e: Exception) {
                Timber.d(e, "Failed to clear legacy DataStore during retirement")
            }
            try {
                val file = produceFile()
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                Timber.d(e, "Failed to delete legacy file during retirement")
            }
        }
    }

    companion object {
        const val DATASTORE_FILE_NAME = "token"
    }
}
