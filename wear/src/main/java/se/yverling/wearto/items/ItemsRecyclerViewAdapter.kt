package se.yverling.wearto.items

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.databinding.ItemsListItemBinding
import javax.inject.Inject

class ItemsRecyclerViewAdapter @Inject constructor() : RecyclerView.Adapter<ItemsRecyclerViewAdapter.ViewHolder>() {
    private var items: List<Item> = mutableListOf()
    private lateinit var events: MutableLiveData<Event>

    fun init(events: MutableLiveData<Event>) {
        this.events = events
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items[position].apply {
            holder.binding.viewModel = ItemsListItemViewModel(name, ColorStateList.valueOf(color), events, items[position])
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<Item>) {
        this.items = items
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemsListItemBinding) : RecyclerView.ViewHolder(binding.root)
}

@BindingAdapter("compoundDrawableTintList")
fun adapter(view: TextView, colorStateList: ColorStateList) {
    view.compoundDrawableTintList = colorStateList
}