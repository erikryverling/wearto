package se.yverling.wearto.wear.feature.items.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import se.yverling.wearto.wear.data.items.ItemsRepository
import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.feature.items.ui.ItemsViewModel.UiState.Loading
import javax.inject.Inject

// TODO Add test when sync in in place
@HiltViewModel
class ItemsViewModel @Inject constructor(repository: ItemsRepository) : ViewModel() {
    internal var uiState: StateFlow<UiState> = repository.getItems().map {
        UiState.Success(it)
    }.stateIn(
        scope = viewModelScope,
        initialValue = Loading,
        started = SharingStarted.WhileSubscribed()
    )

    internal sealed class UiState {
        data object Loading : UiState()
        data class Success(val items: List<Item>) : UiState()
    }
}
