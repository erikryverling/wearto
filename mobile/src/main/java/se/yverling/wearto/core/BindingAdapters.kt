package se.yverling.wearto.core

import android.databinding.BindingAdapter
import android.widget.EditText

@BindingAdapter("focus")
fun setFocus(view: EditText, value: Boolean) {
    if (value) view.requestFocus()
}