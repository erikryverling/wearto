package se.yverling.wearto.mobile.data.token.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import se.yverling.wearto.mobile.data.token.crypto.CryptoManager
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

// TODO: Temporary compatibility code for pre-AEAD upgrades. Remove in a separate release after the supported upgrade window closes.
@Singleton
internal class TokenDataStoreSerializer @Inject constructor(
    private val cryptoManager: CryptoManager,
) : Serializer<String?> {
    override val defaultValue: String? = null

    override suspend fun readFrom(input: InputStream): String? {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) {
            return defaultValue
        }
        return try {
            val decryptedByte = cryptoManager.decrypt(DataInputStream(ByteArrayInputStream(bytes)))
            Json.decodeFromString(
                deserializer = String.serializer(),
                string = String(decryptedByte)
            )
        } catch (e: Exception) {
            Timber.d(e, "Legacy token decrypt/decode failed")
            throw CorruptionException("Failed to read legacy token", e)
        }
    }

    override suspend fun writeTo(t: String?, output: OutputStream) {
        t ?: return

        val jsonEncodedUserData = Json.encodeToString(
            serializer = String.serializer(),
            value = t
        )
        cryptoManager.encrypt(
            bytes = jsonEncodedUserData.encodeToByteArray(),
            outputStream = DataOutputStream(output)
        )
    }
}
