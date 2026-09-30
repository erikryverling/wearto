package se.yverling.wearto.mobile.data.auth

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import se.yverling.wearto.mobile.data.token.TokenRepositoryImpl
import se.yverling.wearto.mobile.data.token.datastore.TokenDataSource

private class TokenRepositoryImplTest {
    private lateinit var dataSource: FakeTokenDataSource
    private lateinit var repository: TokenRepositoryImpl

    @BeforeEach
    fun setUp() {
        dataSource = FakeTokenDataSource()
        repository = TokenRepositoryImpl(dataSource)
    }

    @Test
    fun `getToken should emit token successfully`() = runTest {
        val token = "token"

        repository.setToken(token)

        repository.getToken().first() shouldBe token
    }

    @Test
    fun `hasToken should return true when token exists`() = runTest {
        val token = "token"

        repository.setToken(token)

        repository.hasToken().first().shouldBeTrue()
    }

    @Test
    fun `hasToken should return false when token is absent`() = runTest {
        repository.hasToken().first().shouldBeFalse()
    }

    @Test
    fun `setToken should call data source`() = runTest {
        val token = "token"

        repository.setToken(token)

        dataSource.tokenFlow.first() shouldBe token
    }

    @Test
    fun `clearToken should call data source`() = runTest {
        val token = "token"

        repository.setToken(token)
        repository.clearToken()

        dataSource.tokenFlow.first() shouldBe null
    }
}

private class FakeTokenDataSource(
    initialToken: String? = null,
) : TokenDataSource {
    private val tokenState = MutableStateFlow(initialToken)

    override val tokenFlow: Flow<String?> = tokenState

    override suspend fun persistToken(token: String) {
        tokenState.value = token
    }

    override suspend fun clearToken() {
        tokenState.value = null
    }
}
