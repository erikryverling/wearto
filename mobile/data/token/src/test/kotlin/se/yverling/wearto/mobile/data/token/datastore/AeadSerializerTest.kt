package se.yverling.wearto.mobile.data.token.datastore

import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.InternalSerializationApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@OptIn(InternalSerializationApi::class)
private class AeadSerializerTest {

    private lateinit var primaryAead: Aead
    private lateinit var secondaryAead: Aead
    private val associatedData = TokenDataSourceImpl.ASSOCIATED_DATA

    @BeforeEach
    fun setUp() {
        AeadConfig.register()
        primaryAead = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
        secondaryAead = KeysetHandle.generateNew(KeyTemplates.get("AES256_GCM"))
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    @Test
    fun `writeTo and readFrom should encrypt and decrypt Present token state`() = runTest {
        val serializer = AeadSerializer(
            aead = primaryAead,
            wrappedSerializer = TokenStateSerializer,
            associatedData = associatedData,
        )
        val originalState = StoredTokenState.Present("super_secret_token")
        val outputStream = ByteArrayOutputStream()

        serializer.writeTo(originalState, outputStream)
        val encryptedBytes = outputStream.toByteArray()

        // Verify the raw output is encrypted and does not expose the token in plaintext
        String(encryptedBytes).contains("super_secret_token") shouldBe false

        val inputStream = ByteArrayInputStream(encryptedBytes)
        val decryptedState = serializer.readFrom(inputStream)

        decryptedState shouldBe originalState
    }

    @Test
    fun `writeTo and readFrom should encrypt and decrypt Absent token state`() = runTest {
        val serializer = AeadSerializer(
            aead = primaryAead,
            wrappedSerializer = TokenStateSerializer,
            associatedData = associatedData,
        )
        val originalState = StoredTokenState.Absent
        val outputStream = ByteArrayOutputStream()

        serializer.writeTo(originalState, outputStream)
        val encryptedBytes = outputStream.toByteArray()

        // Verify ciphertext is non-empty
        encryptedBytes.isNotEmpty() shouldBe true

        val inputStream = ByteArrayInputStream(encryptedBytes)
        val decryptedState = serializer.readFrom(inputStream)

        decryptedState shouldBe originalState
    }

    @Test
    fun `readFrom should fail when associated data does not match`() = runTest {
        val serializer = AeadSerializer(
            aead = primaryAead,
            wrappedSerializer = TokenStateSerializer,
            associatedData = associatedData,
        )
        val mismatchedSerializer = AeadSerializer(
            aead = primaryAead,
            wrappedSerializer = TokenStateSerializer,
            associatedData = "mismatched_associated_data".encodeToByteArray(),
        )
        val outputStream = ByteArrayOutputStream()
        serializer.writeTo(StoredTokenState.Present("secret"), outputStream)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        shouldThrow<androidx.datastore.core.CorruptionException> {
            mismatchedSerializer.readFrom(inputStream)
        }
    }

    @Test
    fun `readFrom should fail when decrypted with a different key`() = runTest {
        val serializer = AeadSerializer(
            aead = primaryAead,
            wrappedSerializer = TokenStateSerializer,
            associatedData = associatedData,
        )
        val wrongKeySerializer = AeadSerializer(
            aead = secondaryAead,
            wrappedSerializer = TokenStateSerializer,
            associatedData = associatedData,
        )
        val outputStream = ByteArrayOutputStream()
        serializer.writeTo(StoredTokenState.Present("secret"), outputStream)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        shouldThrow<androidx.datastore.core.CorruptionException> {
            wrongKeySerializer.readFrom(inputStream)
        }
    }

    @Test
    fun `readFrom should fail on empty stream`() = runTest {
        val serializer = AeadSerializer(
            aead = primaryAead,
            wrappedSerializer = TokenStateSerializer,
            associatedData = associatedData,
        )
        val emptyInputStream = ByteArrayInputStream(ByteArray(0))

        shouldThrow<androidx.datastore.core.CorruptionException> {
            serializer.readFrom(emptyInputStream)
        }
    }
}
