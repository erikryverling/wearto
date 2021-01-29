package se.yverling.wearto.items

import android.content.res.ColorStateList
import androidx.lifecycle.MutableLiveData
import se.yverling.wearto.core.entities.Item

class ItemsListItemViewModel(val text: String, val drawableColor: ColorStateList, private val events: MutableLiveData<Event>, val item: Item) {
    fun onClick() {
        events.value = Event.SendItem(item)
    }
}