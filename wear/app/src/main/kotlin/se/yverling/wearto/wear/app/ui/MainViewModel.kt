package se.yverling.wearto.wear.app.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import se.yverling.wearto.wear.app.data.DataLayerRepository
import se.yverling.wearto.wear.data.items.model.Item
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val dataLayerRepository: DataLayerRepository,
) : ViewModel() {
    fun sendItem(item: Item) {
        dataLayerRepository.sendItem(item)
    }
}
