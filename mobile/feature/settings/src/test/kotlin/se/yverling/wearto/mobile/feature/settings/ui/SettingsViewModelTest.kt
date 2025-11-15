package se.yverling.wearto.mobile.feature.settings.ui

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

    @BeforeEach
    fun setup() {
        every { contextMock.packageName } returns "se.yverling.wearto.mobile.app"
        every { contextMock.packageManager.getPackageInfo("se.yverling.wearto.mobile.app", 0).versionName } returns "1.0.0"

        sut = SettingsViewModel(
            context = contextMock,
            settingsRepository = settingsRepositoryMock,
            tokenRepository = tokenRepositoryMock,
            itemsRepository = itemsRepositoryMock,
        )
    }

    @Test
    fun `GIVEN a project WHEN observing projectState THEN a success state is emitted`() = runTest {
        // GIVEN
        val project = Project(id = "1", name = "Project")
        every { settingsRepositoryMock.getProject() } returns flowOf(project)

        // WHEN
        sut.projectState.test {
            // THEN
            awaitItem().shouldBeInstanceOf<ProjectUiState.Loading>()

            val successState = awaitItem().shouldBeInstanceOf<ProjectUiState.Success>()
            successState.project shouldBe "Project"
            successState.versionName shouldBe "1.0.0"
        }
    }

    @Test
    fun `GIVEN projects WHEN observing projectsState THEN a success state is emitted`() = runTest {
        // GIVEN
        val projects = listOf(Project(id = "1", name = "Project"))
        every { settingsRepositoryMock.getProjects() } returns flowOf(projects)

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

        // WHEN
        sut.projectsState.test {
            // THEN
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Error>()
        }
    }

    @Test
    fun `WHEN setting a project THEN the settings repository is called`() = runTest {
        // WHEN
        val project = Project(id = "1", name = "Project")
        sut.setProject(project)

        // THEN
        coVerify { settingsRepositoryMock.setProject(project) }
    }

    @Test
    fun `WHEN logging out THEN the repositories are cleared`() = runTest {
        // WHEN
        sut.logout()

        // THEN
        coVerify { settingsRepositoryMock.clearProject() }
        coVerify { tokenRepositoryMock.clearToken() }
        coVerify { itemsRepositoryMock.clearItems() }
    }
}
