package se.yverling.wearto.mobile.feature.items.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import se.yverling.wearto.mobile.data.items.ItemsRepository
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.feature.items.ui.ItemsViewModel.UiState.Loading
import se.yverling.wearto.mobile.feature.items.ui.ItemsViewModel.UiState.LoggedOut
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ItemsViewModel @Inject constructor(
    tokenRepository: TokenRepository,
    private val itemsRepository: ItemsRepository,
) : ViewModel() {
    internal var uiState: StateFlow<UiState> = tokenRepository.hasToken().flatMapConcat { hasToken ->
        if (!hasToken) {
            flowOf(LoggedOut(hasToken = false))
        } else {
            itemsRepository.getItems().map {
                UiState.Success(items = it)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = Loading,
        started = SharingStarted.WhileSubscribed()
    )

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
    }
}
