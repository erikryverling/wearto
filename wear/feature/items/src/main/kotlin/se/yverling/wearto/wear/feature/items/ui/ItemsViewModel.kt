package se.yverling.wearto.wear.feature.items.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.yverling.wearto.wear.data.items.ItemsRepository
import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.data.items.model.ItemState.Loading
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val itemsRepository: ItemsRepository,
) : ViewModel() {
    internal var uiState: StateFlow<UiState> = itemsRepository.getItems().map {
        UiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = UiState.Loading,
        started = SharingStarted.WhileSubscribed()
    )

    init {
        viewModelScope.launch {
            itemsRepository.resetAllItemState()
        }
    }

    suspend fun setItemStateToLoading(item: Item) {
        itemsRepository.updateItemState(item.name, state = Loading)
    }

    internal sealed class UiState {
        data object Loading : UiState()
        data class Success(val items: List<Item>) : UiState()
    }
}
