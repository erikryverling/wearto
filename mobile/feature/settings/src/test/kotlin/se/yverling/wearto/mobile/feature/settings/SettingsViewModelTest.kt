package se.yverling.wearto.mobile.feature.settings

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
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
    val project1 = Project(id = "0", name = "Project1")
    val project2 = Project(id = "1", name = "Project2")

    @RelaxedMockK
    lateinit var settingsRepositoryMock: SettingsRepository

    @RelaxedMockK
    lateinit var tokenRepositoryMock: TokenRepository

    @RelaxedMockK
    lateinit var itemsRepositoryMock: ItemsRepository

    lateinit var settingsViewModel: SettingsViewModel

    @Test
    fun `projectState should emit Success`() = runTest {
        every { settingsRepositoryMock.getProject() } returns flowOf(project1)

        settingsViewModel = createViewModel()

        settingsViewModel.projectState.test {
            awaitItem().shouldBeInstanceOf<ProjectUiState.Loading>()

            val successItem = awaitItem()

            successItem.shouldBeInstanceOf<ProjectUiState.Success>()
            successItem.project.shouldBe("Project1")
        }
    }

    @Test
    fun `projectsState should emit Success`() = runTest {
        every { settingsRepositoryMock.getProjects() } returns flowOf(listOf(project1, project2))

        settingsViewModel = createViewModel()

        settingsViewModel.projectsState.test {
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()

            val successItem = awaitItem()

            successItem.shouldBeInstanceOf<ProjectsUiState.Success>()
            successItem.projects.shouldBe(listOf(project1, project2))
        }
    }

    @Test
    fun `projectsState should emit LoggedOut on InvalidTokenException`() = runTest {
        every { settingsRepositoryMock.getProjects() } returns flow { throw InvalidTokenException() }

        settingsViewModel = createViewModel()

        settingsViewModel.projectsState.test {
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()

            val successItem = awaitItem()

            successItem.shouldBeInstanceOf<ProjectsUiState.LoggedOut>()
        }
    }

    @Test
    fun `projectsState should emit LoggedOut on NoTokenException`() = runTest {
        every { settingsRepositoryMock.getProjects() } returns flow { throw NoTokenException() }

        settingsViewModel = createViewModel()

        settingsViewModel.projectsState.test {
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()

            val successItem = awaitItem()

            successItem.shouldBeInstanceOf<ProjectsUiState.LoggedOut>()
        }
    }

    @Test
    fun `projectsState should emit Error on other exception`() = runTest {
        every { settingsRepositoryMock.getProjects() } returns flow { throw IllegalArgumentException() }

        settingsViewModel = createViewModel()

        settingsViewModel.projectsState.test {
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()

            val successItem = awaitItem()

            successItem.shouldBeInstanceOf<ProjectsUiState.Error>()
        }
    }

    @Test
    fun `setProject should call settingsRepository successfully`() = runTest {
        settingsViewModel = createViewModel()

        settingsViewModel.setProject(project1)

        coVerify { settingsRepositoryMock.setProject(project1) }
    }

    @Test
    fun `logout should call clear data successfully`() = runTest {
        settingsViewModel = createViewModel()

        settingsViewModel.logout()

        coVerify { settingsRepositoryMock.clearProject() }
        coVerify { tokenRepositoryMock.clearToken() }
        coVerify { itemsRepositoryMock.clearItems() }
    }

    fun createViewModel() = SettingsViewModel(
        settingsRepositoryMock,
        tokenRepositoryMock,
        itemsRepositoryMock
    )
}
