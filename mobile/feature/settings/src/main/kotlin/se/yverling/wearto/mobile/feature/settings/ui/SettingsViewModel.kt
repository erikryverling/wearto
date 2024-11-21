package se.yverling.wearto.mobile.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import se.yverling.wearto.mobile.common.network.exception.InvalidTokenException
import se.yverling.wearto.mobile.data.items.ItemsRepository
import se.yverling.wearto.mobile.data.settings.SettingsRepository
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.feature.settings.exception.NoTokenException
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.ProjectsUiState.LoggedOut
import javax.inject.Inject

@Suppress("OPT_IN_USAGE")
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val tokenRepository: TokenRepository,
    private val itemsRepository: ItemsRepository,
) : ViewModel() {
    internal var projectState = settingsRepository.getProject().map {
        ProjectUiState.Success(it?.name)
    }.stateIn(
        scope = viewModelScope,
        initialValue = ProjectUiState.Loading,
        started = SharingStarted.WhileSubscribed()
    )

    internal var projectsState = settingsRepository.getProjects().map {
        ProjectsUiState.Success(it) as ProjectsUiState
    }.catch { throwable ->
        val state: ProjectsUiState = when (throwable) {
            is InvalidTokenException -> LoggedOut(hasToken = true)
            is NoTokenException -> LoggedOut(hasToken = false)
            else -> ProjectsUiState.Error
        }
        emit(state)
    }.stateIn(
        scope = viewModelScope,
        initialValue = ProjectsUiState.Loading,
        started = SharingStarted.WhileSubscribed()
    )

    suspend fun setProject(project: Project) {
        settingsRepository.setProject(project)
    }

    suspend fun logout() {
        settingsRepository.clearProject()
        tokenRepository.clearToken()
        itemsRepository.clearItems()
    }

    internal sealed class ProjectUiState {
        data object Loading : ProjectUiState()
        data class Success(val project: String?) : ProjectUiState()
    }

    internal sealed class ProjectsUiState {
        data object Loading : ProjectsUiState()
        data class LoggedOut(val hasToken: Boolean) : ProjectsUiState()
        data class Success(val projects: List<Project>) : ProjectsUiState()
        data object Error : ProjectsUiState()
    }
}
