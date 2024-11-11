package se.yverling.wearto.mobile.data.items.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

import se.yverling.wearto.mobile.data.items.model.Item as ItemModel

@Entity(indices = [Index(value = ["name"], unique = true)])
internal data class Item(
    @PrimaryKey(autoGenerate = true)
    val uid: Int? = null,
    val name: String,
)

private fun Item.toModelItem() = ItemModel(uid = uid, name = name)
internal fun List<Item>.toModelItems(): List<ItemModel> = map { it.toModelItem() }
