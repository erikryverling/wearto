package se.yverling.wearto.items

import android.content.res.ColorStateList
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableRecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.databinding.ItemsListItemBinding
import javax.inject.Inject

class ItemsRecyclerViewAdapter @Inject constructor() : androidx.recyclerview.widget.RecyclerView.Adapter<ItemsRecyclerViewAdapter.ViewHolder>() {
    private var items: List<Item> = mutableListOf()
    private lateinit var onItemClick: (Item) -> (Unit)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items[position].apply {
            holder.binding.viewModel = ItemsListItemViewModel(name, ColorStateList.valueOf(color)) {
                onItemClick(items[position])
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<Item>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun onItemClick(function: (Item) -> Unit) {
        this.onItemClick = function
    }

    class ViewHolder(val binding: ItemsListItemBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
}

@BindingAdapter("compoundDrawableTintList")
fun adapter(view: TextView, colorStateList: ColorStateList) {
    view.compoundDrawableTintList = colorStateList
}