package se.yverling.wearto.items

import android.content.res.ColorStateList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import se.yverling.wearto.core.entities.Item
import javax.inject.Inject

class ItemsRecyclerViewAdapter @Inject constructor() : RecyclerView.Adapter<ItemsRecyclerViewAdapter.ViewHolder>() {
    private var items: List<Item> = mutableListOf()
    private lateinit var onItemClick: (Item) -> (Unit)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false) as TextView
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.compoundDrawableTintList = ColorStateList.valueOf(items[position].color)
        holder.textView.text = items[position].name

        holder.textView.setOnClickListener { onItemClick.invoke(items[position]) }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItems(items: List<Item>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun onItemClick(function: (Item) -> Unit) {
        this.onItemClick = function
    }

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}