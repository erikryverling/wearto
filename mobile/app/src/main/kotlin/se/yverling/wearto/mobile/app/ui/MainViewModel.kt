package se.yverling.wearto.mobile.app.ui

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import se.yverling.wearto.mobile.app.data.DataLayerRepository
import se.yverling.wearto.mobile.data.items.ItemsRepository
import se.yverling.wearto.mobile.data.items.model.Item
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val dataLayerRepository: DataLayerRepository,
) : ViewModel() {
    private val mutableUiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Default)
    internal var uiState: StateFlow<UiState> = mutableUiState

    suspend fun sendItems() {
        val items = itemsRepository.getItems().first()
        dataLayerRepository.sendItems(items)
    }

    fun showMessage(@StringRes message: Int) {
        mutableUiState.value = UiState.Message(message)
    }

    fun hideMessage() {
        mutableUiState.value = UiState.Default
    }

    fun getItemsAsCsv() = itemsRepository.getItems().map { items ->
        items.joinToString(DELIMITER) { item -> item.name }
    }

    fun replaceWithItemsFromCsv(csv: String) {
        viewModelScope.launch {
            itemsRepository.clearItems()

            val importedItems = csv.splitToSequence(DELIMITER).map { Item(name = it) }.toList()
            itemsRepository.setItems(importedItems)
        }
    }

    internal sealed class UiState {
        data class Message(@StringRes val message: Int) : UiState()
        data object Default : UiState()
    }

    private companion object {
        private const val DELIMITER = ","
    }
}
