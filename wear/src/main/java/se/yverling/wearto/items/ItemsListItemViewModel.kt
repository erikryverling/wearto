package se.yverling.wearto.items

import android.content.res.ColorStateList

class ItemsListItemViewModel(val text: String, val drawableColor: ColorStateList, val onClickParam: () -> Unit) {
    fun onClick() = onClickParam()
}