package se.yverling.wearto.chars

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import se.yverling.wearto.databinding.CharsListItemBinding
import javax.inject.Inject

class CharsRecyclerViewAdapter @Inject constructor() : RecyclerView.Adapter<CharsRecyclerViewAdapter.ViewHolder>() {
    private var chars: List<Char> = mutableListOf()
    private lateinit var onCharClick: (Char) -> (Unit)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(CharsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        chars[position].apply {
            holder.binding.viewModel = CharsListItemViewModel(this) {
                onCharClick(chars[position])
            }
        }
    }

    override fun getItemCount(): Int = chars.size

    fun setChars(chars: List<Char>) {
        this.chars = chars
        notifyDataSetChanged()
    }

    fun onCharClick(function: (Char) -> Unit) {
        this.onCharClick = function
    }

    class ViewHolder(val binding: CharsListItemBinding) : RecyclerView.ViewHolder(binding.root)
}

@BindingAdapter("compoundDrawableTintList")
fun adapter(view: TextView, colorStateList: ColorStateList) {
    view.compoundDrawableTintList = colorStateList
}