package se.yverling.wearto.mobile.feature.settings

import app.cash.turbine.test
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import se.yverling.wearto.mobile.common.network.exception.InvalidTokenException
import se.yverling.wearto.mobile.common.network.exception.NoTokenException
import se.yverling.wearto.mobile.data.items.ItemsRepository
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.data.settings.SettingsRepository
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.ProjectUiState
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.ProjectsUiState
import se.yverling.wearto.test.MainDispatcherExtension

@ExtendWith(MainDispatcherExtension::class)
private class SettingsViewModelTest {
    val project1 = Project(id = "0", name = "Project1")
    val project2 = Project(id = "1", name = "Project2")

    @Test
    fun `projectState should emit Success`() = runTest {
        val repository = FakeSettingsRepository(initialProject = project1)
        val viewModel = createViewModel(settingsRepository = repository)

        viewModel.projectState.test {
            awaitItem().shouldBeInstanceOf<ProjectUiState.Loading>()

            val successItem = awaitItem()
            successItem.shouldBeInstanceOf<ProjectUiState.Success>()
            successItem.project.shouldBe("Project1")
        }
    }

    @Test
    fun `projectsState should emit Success`() = runTest {
        val repository = FakeSettingsRepository(projectsFlow = flowOf(listOf(project1, project2)))
        val viewModel = createViewModel(settingsRepository = repository)

        viewModel.projectsState.test {
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()

            val successItem = awaitItem()
            successItem.shouldBeInstanceOf<ProjectsUiState.Success>()
            successItem.projects.shouldBe(listOf(project1, project2))
        }
    }

    @Test
    fun `projectsState should emit LoggedOut on InvalidTokenException`() = runTest {
        val repository = FakeSettingsRepository(projectsFlow = flow { throw InvalidTokenException() })
        val viewModel = createViewModel(settingsRepository = repository)

        viewModel.projectsState.test {
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()

            val successItem = awaitItem()
            successItem.shouldBeInstanceOf<ProjectsUiState.LoggedOut>()
            successItem.hasToken.shouldBeTrue()
        }
    }

    @Test
    fun `projectsState should emit LoggedOut on NoTokenException`() = runTest {
        val repository = FakeSettingsRepository(projectsFlow = flow { throw NoTokenException() })
        val viewModel = createViewModel(settingsRepository = repository)

        viewModel.projectsState.test {
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()

            val successItem = awaitItem()
            successItem.shouldBeInstanceOf<ProjectsUiState.LoggedOut>()
            successItem.hasToken.shouldBeFalse()
        }
    }

    @Test
    fun `projectsState should emit Error on other exception`() = runTest {
        val repository = FakeSettingsRepository(projectsFlow = flow { throw IllegalArgumentException() })
        val viewModel = createViewModel(settingsRepository = repository)

        viewModel.projectsState.test {
            awaitItem().shouldBeInstanceOf<ProjectsUiState.Loading>()

            val successItem = awaitItem()
            successItem.shouldBeInstanceOf<ProjectsUiState.Error>()
        }
    }

    @Test
    fun `setProject should call settingsRepository successfully`() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = createViewModel(settingsRepository = repository)

        viewModel.setProject(project1)

        repository.lastSavedProject shouldBe project1
    }

    @Test
    fun `logout should call clear data successfully`() = runTest {
        val settingsRepository = FakeSettingsRepository()
        val tokenRepository = FakeTokenRepository()
        val itemsRepository = FakeItemsRepository()
        val viewModel = createViewModel(
            settingsRepository = settingsRepository,
            tokenRepository = tokenRepository,
            itemsRepository = itemsRepository,
        )

        viewModel.logout()

        settingsRepository.cleared.shouldBeTrue()
        tokenRepository.cleared.shouldBeTrue()
        itemsRepository.cleared.shouldBeTrue()
    }

    private fun createViewModel(
        settingsRepository: SettingsRepository = FakeSettingsRepository(),
        tokenRepository: TokenRepository = FakeTokenRepository(),
        itemsRepository: ItemsRepository = FakeItemsRepository(),
    ) = SettingsViewModel(
        settingsRepository,
        tokenRepository,
        itemsRepository
    )
}

private class FakeSettingsRepository(
    initialProject: Project? = null,
    private val projectsFlow: Flow<List<Project>> = flowOf(emptyList()),
) : SettingsRepository {
    private val projectFlow = MutableStateFlow(initialProject)
    var lastSavedProject: Project? = null
        private set
    var cleared = false
        private set

    override fun getProject(): Flow<Project?> = projectFlow

    override fun getProjects(): Flow<List<Project>> = projectsFlow

    override suspend fun setProject(project: Project) {
        lastSavedProject = project
        projectFlow.value = project
    }

    override suspend fun clearProject() {
        cleared = true
        projectFlow.value = null
    }
}

private class FakeTokenRepository : TokenRepository {
    var cleared = false
        private set

    override fun getToken(): Flow<String?> = flowOf(null)

    override suspend fun setToken(token: String) {}

    override suspend fun clearToken() {
        cleared = true
    }

    override fun hasToken(): Flow<Boolean> = flowOf(!cleared)
}

private class FakeItemsRepository : ItemsRepository {
    var cleared = false
        private set

    override fun getItems(): Flow<List<Item>> = flowOf(emptyList())

    override suspend fun setItem(item: Item) {}

    override suspend fun setItems(items: List<Item>) {}

    override suspend fun deleteItem(item: Item) {}

    override suspend fun clearItems() {
        cleared = true
    }
}
