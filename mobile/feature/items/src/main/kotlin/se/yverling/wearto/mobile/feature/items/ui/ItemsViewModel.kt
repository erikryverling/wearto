package se.yverling.wearto.mobile.feature.items.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import se.yverling.wearto.mobile.data.items.ItemsRepository
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.data.token.UnrecoverableTokenException
import se.yverling.wearto.mobile.feature.items.ui.ItemsViewModel.UiState.Loading
import se.yverling.wearto.mobile.feature.items.ui.ItemsViewModel.UiState.LoggedOut
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val itemsRepository: ItemsRepository,
) : ViewModel() {
    private val retryTrigger = MutableStateFlow(0)

    internal val uiState: StateFlow<UiState> = retryTrigger.flatMapLatest {
        tokenRepository.hasToken().flatMapConcat { hasToken ->
            if (!hasToken) {
                flowOf(LoggedOut(hasToken = false))
            } else {
                itemsRepository.getItems().map {
                    UiState.Success(items = it)
                }
            }
        }.catch { throwable ->
            when (throwable) {
                is UnrecoverableTokenException -> {
                    emit(UiState.CredentialRecovery)
                }
                is IOException -> {
                    emit(UiState.TransientError)
                }
                else -> {
                    emit(UiState.CredentialRecovery)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = Loading,
        started = SharingStarted.WhileSubscribed()
    )

    internal fun retry() {
        retryTrigger.value += 1
    }

    internal suspend fun setItem(item: Item) {
        itemsRepository.setItem(item)
    }

    internal suspend fun deleteItem(item: Item) {
        itemsRepository.deleteItem(item)
    }

    internal sealed class UiState {
        data object Loading : UiState()
        data class LoggedOut(val hasToken: Boolean) : UiState()
        data class Success(val items: List<Item>) : UiState()
        data object CredentialRecovery : UiState()
        data object TransientError : UiState()
    }
}
