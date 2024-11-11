package se.yverling.wearto.mobile.app.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import se.yverling.wearto.mobile.data.items.ItemsRepository
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val itemsRepository: ItemsRepository,
) : ViewModel() {
    fun getItems() = itemsRepository.getItems()
}
