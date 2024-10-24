import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.feature.login.ui.LoginViewModel
import se.yverling.wearto.test.MainDispatcherExtension

@ExtendWith(MockKExtension::class)
@ExtendWith(MainDispatcherExtension::class)
class LoginViewModelTest {
    @RelaxedMockK
    lateinit var tokenRepositoryMock: TokenRepository

    @Test
    fun `setToken should call repository`() = runTest {
        val viewModel = LoginViewModel(tokenRepositoryMock)

        viewModel.setToken("Token")

        coVerify { tokenRepositoryMock.setToken("Token") }
    }
}
