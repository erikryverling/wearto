package se.yverling.wearto.items

import android.content.res.ColorStateList

class ItemsListItemViewModel(
        val itemName: String,
        val projectName: String,
        val colorStateList: ColorStateList,
        val onItemClickParam: () -> Unit) {

    fun onItemClick() {
        onItemClickParam()
    }
}