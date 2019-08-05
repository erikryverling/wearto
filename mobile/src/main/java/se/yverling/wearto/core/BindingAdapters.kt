package se.yverling.wearto.core

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.SpinnerAdapter

@BindingAdapter("focus")
fun setFocus(view: EditText, value: Boolean) {
    if (value) view.requestFocus()
}

@BindingAdapter("spinnerAdapter")
fun setAdapter(view: Spinner, adapter: SpinnerAdapter) {
    view.adapter = adapter
}

@BindingAdapter(value = ["selectedValue", "selectedValueAttrChanged"], requireAll = false)
fun bindSpinnerData(spinner: Spinner, newSelectedValue: String?, newTextAttrChanged: InverseBindingListener) {
    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            newTextAttrChanged.onChange()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    if (newSelectedValue != null) {
        val position = (spinner.adapter as ArrayAdapter<String>).getPosition(newSelectedValue)
        spinner.setSelection(position, true)
    }
}

@InverseBindingAdapter(attribute = "selectedValue", event = "selectedValueAttrChanged")
fun captureSelectedValue(spinner: Spinner): String {
    return spinner.selectedItem as String
}