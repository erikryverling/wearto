package se.yverling.wearto.mobile.data.auth

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.data.token.datastore.TokenDataSource
import se.yverling.wearto.mobile.data.token.TokenRepositoryImpl

@ExtendWith(MockKExtension::class)
class TokenRepositoryImplTest {
    @RelaxedMockK
    internal lateinit var dataStoreMock: TokenDataSource

    private lateinit var repository: TokenRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = TokenRepositoryImpl(dataStoreMock)
    }

    @Test
    fun `should get token successfully`() = runTest {
        val token = "token"

        repository.setToken(token)

        repository.getToken().collect {
            it.shouldBe(token)
        }
    }

    @Test
    fun `should set token successfully`() = runTest {
        val token = "token"

        repository.setToken(token)

        coVerify { dataStoreMock.persistToken(token) }
    }

    @Test
    fun `should clear token successfully`() = runTest {
        repository.clearToken()

        coVerify { dataStoreMock.clearToken() }
    }

    @Test
    fun `should return has token successfully`() = runTest {
        val token = "token"

        repository.setToken(token)

        repository.hasToken().collect {
            it.shouldBeTrue()
        }
    }
}
