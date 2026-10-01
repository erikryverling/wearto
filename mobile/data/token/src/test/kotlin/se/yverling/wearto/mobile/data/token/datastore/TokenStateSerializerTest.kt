package se.yverling.wearto.mobile.data.token.datastore

import androidx.datastore.core.CorruptionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.InternalSerializationApi
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@OptIn(InternalSerializationApi::class)
private class TokenStateSerializerTest {

    @Test
    fun `defaultValue should be Unset`() {
        TokenStateSerializer.defaultValue shouldBe StoredTokenState.Unset
    }

    @Test
    fun `writeTo and readFrom should serialize and deserialize Present state`() = runTest {
        val outputStream = ByteArrayOutputStream()
        val originalState = StoredTokenState.Present("test-token-123")

        TokenStateSerializer.writeTo(originalState, outputStream)
        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val deserializedState = TokenStateSerializer.readFrom(inputStream)

        deserializedState shouldBe originalState
    }

    @Test
    fun `writeTo and readFrom should serialize and deserialize Absent state`() = runTest {
        val outputStream = ByteArrayOutputStream()
        val originalState = StoredTokenState.Absent

        TokenStateSerializer.writeTo(originalState, outputStream)
        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val deserializedState = TokenStateSerializer.readFrom(inputStream)

        deserializedState shouldBe originalState
    }

    @Test
    fun `readFrom should throw CorruptionException when input stream is empty`() = runTest {
        val emptyInputStream = ByteArrayInputStream(ByteArray(0))

        shouldThrow<CorruptionException> {
            TokenStateSerializer.readFrom(emptyInputStream)
        }
    }

    @Test
    fun `readFrom should throw CorruptionException when input is invalid JSON`() = runTest {
        val invalidInputStream = ByteArrayInputStream("{ not valid json }".encodeToByteArray())

        shouldThrow<CorruptionException> {
            TokenStateSerializer.readFrom(invalidInputStream)
        }
    }
}
