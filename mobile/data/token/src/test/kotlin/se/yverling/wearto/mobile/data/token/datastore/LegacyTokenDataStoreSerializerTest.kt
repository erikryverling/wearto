package se.yverling.wearto.mobile.data.token.datastore

import androidx.datastore.core.CorruptionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import se.yverling.wearto.mobile.data.token.crypto.CryptoManager
import java.io.ByteArrayInputStream
import java.security.GeneralSecurityException

private class LegacyTokenDataStoreSerializerTest {

    private val cryptoManager: CryptoManager = mockk()
    private val serializer = TokenDataStoreSerializer(cryptoManager)

    @Test
    fun `readFrom should return null when input stream is empty`() = runTest {
        val emptyStream = ByteArrayInputStream(ByteArray(0))

        val result = serializer.readFrom(emptyStream)

        result.shouldBeNull()
    }

    @Test
    fun `readFrom should throw CorruptionException when decryption fails`() = runTest {
        val streamWithBytes = ByteArrayInputStream(byteArrayOf(1, 2, 3, 4))
        every { cryptoManager.decrypt(any()) } throws GeneralSecurityException("Invalid key")

        shouldThrow<CorruptionException> {
            serializer.readFrom(streamWithBytes)
        }
    }

    @Test
    fun `readFrom should successfully deserialize decrypted JSON token`() = runTest {
        val streamWithBytes = ByteArrayInputStream(byteArrayOf(1, 2, 3, 4))
        val jsonToken = "\"secret-legacy-token\"".toByteArray()
        every { cryptoManager.decrypt(any()) } returns jsonToken

        val result = serializer.readFrom(streamWithBytes)

        result shouldBe "secret-legacy-token"
    }
}
