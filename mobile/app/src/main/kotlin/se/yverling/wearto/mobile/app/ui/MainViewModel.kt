package se.yverling.wearto.mobile.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.yverling.wearto.mobile.app.data.DataLayerRepository
import se.yverling.wearto.mobile.data.items.ItemsRepository
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val dataLayerRepository: DataLayerRepository,
) : ViewModel() {
    suspend fun sendItems() {
        val items = itemsRepository.getItems().first()
        dataLayerRepository.sendItems(items)
    }
}
