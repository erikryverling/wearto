package se.yverling.wearto.mobile.feature.login.ui

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.test.MainDispatcherExtension

@ExtendWith(MainDispatcherExtension::class)
private class LoginViewModelTest {
    @Test
    fun `setToken should call TokenRepository`() = runTest {
        val fakeTokenRepository = FakeTokenRepository()
        val viewModel = LoginViewModel(fakeTokenRepository)

        viewModel.setToken("Token")

        fakeTokenRepository.lastSetToken shouldBe "Token"
    }
}

private class FakeTokenRepository : TokenRepository {
    var lastSetToken: String? = null
        private set

    override fun getToken(): Flow<String?> = flowOf(lastSetToken)

    override suspend fun setToken(token: String) {
        lastSetToken = token
    }

    override suspend fun clearToken() {
        lastSetToken = null
    }

    override fun hasToken(): Flow<Boolean> = flowOf(lastSetToken != null)
}
