package se.yverling.wearto.wear.feature.items.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import se.yverling.wearto.wear.data.items.ItemsRepository
import se.yverling.wearto.wear.data.items.model.Item
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor(repository: ItemsRepository) : ViewModel() {
    init {
        viewModelScope.launch {
            repository.setItems(listOf(Item(name = "A"), Item(name = "B")))
            repository.getItems().collect {
                Timber.tag("WearTo").d(it.toString())
            }
        }
    }
}
