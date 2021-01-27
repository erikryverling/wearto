package se.yverling.wearto.items

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import se.yverling.wearto.core.entities.ItemWithProject
import se.yverling.wearto.databinding.ItemsListItemBinding
import javax.inject.Inject

class ItemsRecyclerViewAdapter @Inject constructor() : RecyclerView.Adapter<ItemsRecyclerViewAdapter.ViewHolder>() {
    private var itemsWithProject: List<ItemWithProject> = mutableListOf()
    private lateinit var itemToEditEvents: MutableLiveData<String>

    fun init(itemToEditEvents: MutableLiveData<String>) {
        this.itemToEditEvents = itemToEditEvents
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        itemsWithProject[position].apply {
            holder.binding.viewModel = ItemsListItemViewModel(item.name, item.uuid, project.name, ColorStateList.valueOf(project.color), itemToEditEvents)
        }
    }

    override fun getItemCount(): Int = itemsWithProject.size

    fun setItems(itemsWithProject: List<ItemWithProject>) {
        this.itemsWithProject = itemsWithProject
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemsListItemBinding) : RecyclerView.ViewHolder(binding.root)
}