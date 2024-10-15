package se.yverling.wearto.mobile.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.data.settings.model.Project
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.UiState.Error
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.UiState.LoggedOut
import se.yverling.wearto.mobile.feature.settings.ui.SettingsViewModel.UiState.Success
import se.yverling.wearto.mobile.common.network.exception.InvalidTokenException
import se.yverling.wearto.mobile.data.settings.SettingsRepository
import se.yverling.wearto.mobile.feature.settings.exception.NoTokenException
import javax.inject.Inject

@Suppress("OPT_IN_USAGE")
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    internal var uiState: StateFlow<UiState>

    init {
        // TODO Move this to ItemsScreen when implemented or somewhere else?
        uiState = tokenRepository
            .hasToken()
            .onEach { hasToken ->
                if (!hasToken) throw NoTokenException()
            }
            .flatMapConcat {
                combine(
                    settingsRepository.getProject(),
                    settingsRepository.getProjects(),
                ) { project, projects ->
                    Success(project = project, projects = projects) as UiState
                }
            }.catch {
                val state = when (it) {
                    is InvalidTokenException -> LoggedOut(hasToken = true)
                    is NoTokenException -> LoggedOut(hasToken = false)
                    else -> Error
                }
                emit(state)
            }.stateIn(
                scope = viewModelScope,
                initialValue = UiState.Loading,
                started = SharingStarted.WhileSubscribed(3000)
            )
    }

    fun setProject(project: String) {
        viewModelScope.launch {
            settingsRepository.setProject(project)
        }
    }

    suspend fun logout() {
        settingsRepository.clearProject()
        tokenRepository.clearToken()
    }

    internal sealed class UiState {
        data object Loading : UiState()

        data class LoggedOut(val hasToken: Boolean) : UiState()

        data object Error : UiState()

        data class Success(
            val project: String?,
            val projects: List<Project>,
        ) : UiState()
    }
}
