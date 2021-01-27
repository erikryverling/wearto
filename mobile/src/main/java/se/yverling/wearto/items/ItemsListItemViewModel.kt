package se.yverling.wearto.items

import android.content.res.ColorStateList
import androidx.lifecycle.MutableLiveData

class ItemsListItemViewModel(
        val itemName: String,
        val itemUuid: String,
        val projectName: String,
        val colorStateList: ColorStateList,
        val itemToEditEvents: MutableLiveData<String>) {

    fun onItemClick() {
        itemToEditEvents.value = itemUuid
    }
}