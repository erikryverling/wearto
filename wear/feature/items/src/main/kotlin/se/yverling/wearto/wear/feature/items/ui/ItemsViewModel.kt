package se.yverling.wearto.wear.feature.items.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.yverling.wearto.wear.data.items.ItemStatusHolder
import se.yverling.wearto.wear.data.items.ItemsRepository
import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.data.items.model.ItemState
import se.yverling.wearto.wear.data.items.sync.ItemSyncClient
import se.yverling.wearto.wear.feature.items.model.ItemUiModel
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor(
    itemsRepository: ItemsRepository,
    private val itemStatusHolder: ItemStatusHolder,
    private val itemSyncClient: ItemSyncClient,
) : ViewModel() {
    internal var uiState: StateFlow<UiState> = combine(
        itemsRepository.getItems(),
        itemStatusHolder.statuses,
    ) { items, statuses ->
        UiState.Success(
            items.map { item ->
                ItemUiModel(
                    item = item,
                    state = statuses[item.name] ?: ItemState.Init,
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = UiState.Loading,
        started = SharingStarted.WhileSubscribed(),
    )

    fun sendItem(item: Item) {
        itemStatusHolder.setLoading(item.name)
        viewModelScope.launch {
            val result = itemSyncClient.sendItem(item.name)
            if (result.isFailure) {
                itemStatusHolder.setConfirmed(item.name, isSuccess = false)
            }
        }
    }

    internal sealed class UiState {
        data object Loading : UiState()
        data class Success(val items: List<ItemUiModel>) : UiState()
    }
}
