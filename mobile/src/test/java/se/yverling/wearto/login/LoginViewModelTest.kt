package se.yverling.wearto.login

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import assertk.assert
import assertk.assertions.isEqualTo
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import se.yverling.wearto.auth.TokenManager
import se.yverling.wearto.core.db.DatabaseClient
import se.yverling.wearto.login.LoginViewModel.Events.*
import se.yverling.wearto.sync.network.NetworkClient
import se.yverling.wearto.sync.network.dtos.Project
import se.yverling.wearto.sync.network.dtos.SyncResponse
import se.yverling.wearto.test.RxRule
import se.yverling.wearto.test.valueIsEqualTo
import se.yverling.wearto.test.whenever
import java.net.UnknownHostException

private const val ACCESS_TOKEN = "test-access-token"

/**
 * Tests the ViewModel behaviour (for UI tests see LoginUiTest)
 */
class LoginViewModelTest {
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()

    @get:Rule
    var liveDataRule = InstantTaskExecutorRule()

    @get:Rule
    var rxRule: RxRule = RxRule()

    @Mock
    lateinit var applicationMock: Application
    @Mock
    lateinit var networkClientMock: NetworkClient
    @Mock
    lateinit var tokenManagerMock: TokenManager
    @Mock
    lateinit var databaseClientMock: DatabaseClient
    @Mock
    lateinit var analyticsMock: FirebaseAnalytics

    lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        whenever(tokenManagerMock.getAccessToken()).thenReturn(Maybe.empty())

        viewModel = LoginViewModel(
                applicationMock,
                tokenManagerMock,
                networkClientMock,
                databaseClientMock,
                analyticsMock)
    }

    @Test
    fun `Should login successfully`() {
        val projects = listOf(Project(1, "Project1", 0))

        whenever(networkClientMock.getProjects(ACCESS_TOKEN)).thenReturn(Single.just(SyncResponse(projects)))
        whenever(databaseClientMock.replaceAllProjects(projects)).thenReturn(Completable.complete())
        whenever(tokenManagerMock.persistToken(anyString())).thenReturn(Completable.complete())

        viewModel.accessToken.set(ACCESS_TOKEN)

        viewModel.login()

        verify(databaseClientMock).replaceAllProjects(projects)
        verify(tokenManagerMock).persistToken(ACCESS_TOKEN)

        assert(viewModel.accessToken).valueIsEqualTo("")
        assert(viewModel.isLoggingIn).valueIsEqualTo(false)
        assert(viewModel.events.value).isEqualTo(START_ITEMS_ACTIVITY_EVENT)
    }

    @Test
    fun `Should show ErrorMessage when NetworkErrorEvent occurs`() {
        whenever(networkClientMock.getProjects(ACCESS_TOKEN)).thenReturn(Single.error(UnknownHostException()))

        // This is to make sure the Rx chain doesn't throw an NPE
        whenever(tokenManagerMock.persistToken(ACCESS_TOKEN)).thenReturn(Completable.complete())

        viewModel.accessToken.set(ACCESS_TOKEN)

        viewModel.login()

        assert(viewModel.events.value).isEqualTo(LOGIN_FAILED_DUE_TO_NETWORK_DIALOG_EVENT)
        assert(viewModel.accessToken).valueIsEqualTo(ACCESS_TOKEN)
        assert(viewModel.isLoggingIn).valueIsEqualTo(false)
    }


    @Test
    fun `Should show ErrorMessage when GeneralErrorEvent occurs`() {
        whenever(networkClientMock.getProjects(ACCESS_TOKEN)).thenReturn(Single.error(RuntimeException()))

        // This is to make sure the Rx chain doesn't throw an NPE
        whenever(tokenManagerMock.persistToken(ACCESS_TOKEN)).thenReturn(Completable.error(RuntimeException()))

        viewModel.accessToken.set(ACCESS_TOKEN)

        viewModel.login()

        assert(viewModel.events.value).isEqualTo(LOGIN_FAILED_DUE_TO_GENERAL_ERROR_EVENT)
        assert(viewModel.accessToken).valueIsEqualTo(ACCESS_TOKEN)
        assert(viewModel.isLoggingIn).valueIsEqualTo(false)
    }
}