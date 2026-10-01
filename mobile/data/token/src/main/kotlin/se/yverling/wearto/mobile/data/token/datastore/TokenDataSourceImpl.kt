package se.yverling.wearto.mobile.data.token.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.Aead
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import se.yverling.wearto.mobile.data.token.crypto.TokenAead
import java.io.File
import javax.inject.Inject

@OptIn(InternalSerializationApi::class)
internal class TokenDataSourceImpl(
    private val dataStore: DataStore<StoredTokenState>,
) : TokenDataSource {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        @TokenAead aead: Aead,
    ) : this(
        dataStore = createDataStore(
            aead = aead,
            produceFile = { context.dataStoreFile(DATASTORE_FILE_NAME) },
        )
    )

    override val stateFlow: Flow<StoredTokenState> = dataStore.data

    override val tokenFlow: Flow<String?> = stateFlow.map { state ->
        when (state) {
            is StoredTokenState.Present -> state.token
            is StoredTokenState.Absent, is StoredTokenState.Unset -> null
        }
    }

    override suspend fun persistToken(token: String) {
        withContext(Dispatchers.IO) {
            dataStore.updateData { StoredTokenState.Present(token) }
        }
    }

    override suspend fun clearToken() {
        withContext(Dispatchers.IO) {
            dataStore.updateData { StoredTokenState.Absent }
        }
    }

    companion object {
        const val DATASTORE_FILE_NAME = "token_v1.json"
        val ASSOCIATED_DATA = "token_v1.json".encodeToByteArray()

        fun createDataStore(
            aead: Aead,
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile: () -> File,
        ): DataStore<StoredTokenState> {
            return DataStoreFactory.create(
                serializer = AeadSerializer(
                    aead = aead,
                    wrappedSerializer = TokenStateSerializer,
                    associatedData = ASSOCIATED_DATA,
                ),
                scope = scope,
                produceFile = produceFile,
            )
        }
    }
}
