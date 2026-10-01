package se.yverling.wearto.mobile.data.token.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal object TokenStateSerializer : Serializer<StoredTokenState> {
    override val defaultValue: StoredTokenState = StoredTokenState.Unset

    override suspend fun readFrom(input: InputStream): StoredTokenState {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) {
            throw CorruptionException("Cannot read token state from empty stream")
        }
        return try {
            Json.decodeFromString(StoredTokenState.serializer(), bytes.decodeToString())
        } catch (e: SerializationException) {
            throw CorruptionException("Failed to deserialize token state", e)
        } catch (e: IllegalArgumentException) {
            throw CorruptionException("Failed to decode token state JSON", e)
        }
    }

    override suspend fun writeTo(t: StoredTokenState, output: OutputStream) {
        val json = Json.encodeToString(StoredTokenState.serializer(), t)
        output.write(json.encodeToByteArray())
    }
}
