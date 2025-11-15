package se.yverling.wearto.mobile.feature.settings

import android.content.Context
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.common.network.exception.InvalidTokenException
import se.yverling.wearto.mobile.data.items.ItemsRepository
import se.yverling.wearto.mobile.data.settings.SettingsRepository
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.feature.settings.exception.NoTokenException
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.ProjectUiState
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.ProjectsUiState
import se.yverling.wearto.test.MainDispatcherExtension

@ExtendWith(MockKExtension::class)
@ExtendWith(MainDispatcherExtension::class)
private class SettingsViewModelTest {
    @RelaxedMockK
    private lateinit var contextMock: Context

    @RelaxedMockK
    private lateinit var settingsRepositoryMock: SettingsRepository

    @RelaxedMockK
    private lateinit var tokenRepositoryMock: TokenRepository

    @RelaxedMockK
    private lateinit var itemsRepositoryMock: ItemsRepository

    private lateinit var sut: SettingsViewModel

    @Test
    fun `GIVEN a project WHEN observing projectState THEN a success state is emitted`() = runTest {
        val project = Project(id = "1", name = "Project")
        every { settingsRepositoryMock.getProject() } returns flowOf(project)

        sut = createViewModel()

        // WHEN
        sut.projectState.test {
            // THEN
            awaitItem().shouldBeInstanceOf<ProjectUiState.Loading>()

            val successState = awaitItem().shouldBeInstanceOf<ProjectUiState.Success>()
            successState.project shouldBe "Project"
        }
    }

    @Test
    fun `GIVEN projects WHEN observing projectsState THEN a success state is emitted`() = runTest {
        // GIVEN
        val projects = listOf(Project(id = "1", name = "Project"))
        every { settingsRepositoryMock.getProjects() } returns flowOf(projects)

        sut = createViewModel()

        // WHEN
        sut.projectsState.test {
            // THEN
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()

            val successState = awaitItem().shouldBeInstanceOf<ProjectsUiState.Success>()
            successState.projects shouldBe projects
        }
    }

    @Test
    fun `GIVEN InvalidTokenException WHEN observing projectsState THEN a logged out state is emitted`() = runTest {
        // GIVEN
        every { settingsRepositoryMock.getProjects() } returns flow { throw InvalidTokenException() }

        sut = createViewModel()

        // WHEN
        sut.projectsState.test {
            // THEN
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()
            awaitItem().shouldBeInstanceOf<ProjectsUiState.LoggedOut>()
        }
    }

    @Test
    fun `GIVEN NoTokenException WHEN observing projectsState THEN a logged out state is emitted`() = runTest {
        // GIVEN
        every { settingsRepositoryMock.getProjects() } returns flow { throw NoTokenException() }

        sut = createViewModel()

        // WHEN
        sut.projectsState.test {
            // THEN
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()
            awaitItem().shouldBeInstanceOf<ProjectsUiState.LoggedOut>()
        }
    }

    @Test
    fun `GIVEN an unexpected exception WHEN observing projectsState THEN an error state is emitted`() = runTest {
        // GIVEN
        every { settingsRepositoryMock.getProjects() } returns flow { throw RuntimeException() }

        sut = createViewModel()

        // WHEN
        sut.projectsState.test {
            // THEN
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Error>()
        }
    }

    @Test
    fun `WHEN setting a project THEN the settings repository is called`() = runTest {
        sut = createViewModel()

        // WHEN
        val project = Project(id = "1", name = "Project")
        sut.setProject(project)

        // THEN
        coVerify { settingsRepositoryMock.setProject(project) }
    }

    @Test
    fun `WHEN logging out THEN the repositories are cleared`() = runTest {
        sut = createViewModel()

        // WHEN
        sut.logout()

        // THEN
        coVerify { settingsRepositoryMock.clearProject() }
        coVerify { tokenRepositoryMock.clearToken() }
        coVerify { itemsRepositoryMock.clearItems() }
    }

    private fun createViewModel() = SettingsViewModel(
        context = contextMock,
        settingsRepository = settingsRepositoryMock,
        tokenRepository = tokenRepositoryMock,
        itemsRepository = itemsRepositoryMock,
    )
}
