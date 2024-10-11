package se.yverling.wearto.mobile.data.auth

import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.data.auth.datastore.TokenDataSource

@ExtendWith(MockKExtension::class)
class AuthRepositoryImplTest {
    @RelaxedMockK
    internal lateinit var dataStoreMock: TokenDataSource

    private lateinit var repository: AuthRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = AuthRepositoryImpl(dataStoreMock)
    }

    @Test
    fun login() = runTest {
        val token = "token"

        repository.login(token)

        coVerify { dataStoreMock.persistToken(token) }
    }

    @Test
    fun logout() = runTest {
        repository.logout()

        coVerify { dataStoreMock.clearToken() }
    }

    @Test
    fun getToken() = runTest {
        val token = "token"

        repository.login(token)

        repository.getToken().collect {
            it.shouldBe(token)
        }
    }
}
