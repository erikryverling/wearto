package se.yverling.wearto.mobile.data.token.datastore

import timber.log.Timber
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.core.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import se.yverling.wearto.mobile.data.token.crypto.CryptoManager

@Singleton
internal class TokenDataStoreSerializer @Inject constructor(
    private val cryptoManager: CryptoManager,
) : Serializer<String?> {
    override val defaultValue: String? = null

    override suspend fun readFrom(input: InputStream): String? {
        return try {
            val decryptedByte = cryptoManager.decrypt(DataInputStream(input))
            Json.decodeFromString(
                deserializer = String.serializer(),
                string = String(decryptedByte)
            )
        } catch (e: NegativeArraySizeException) {
            Timber.d(e, "UserPreferences decrypt failed")
            defaultValue
        } catch (e: Exception) {
            Timber.d(e, "UserPreferences serialization failed")
            defaultValue
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
