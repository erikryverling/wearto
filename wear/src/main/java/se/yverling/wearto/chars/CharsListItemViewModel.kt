package se.yverling.wearto.chars

class CharsListItemViewModel(val char: Char, val onClickParam: () -> Unit) {
    fun onClick() = onClickParam()
}