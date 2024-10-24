package se.yverling.wearto.mobile.data.items.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class Item(
    @PrimaryKey(autoGenerate = true)
    val uid: Int? = null,
    val name: String,
)

internal fun Item.toModelItem() = se.yverling.wearto.mobile.data.items.model.Item(uid = uid, name = name)
internal fun List<Item>.toModelItems(): List<se.yverling.wearto.mobile.data.items.model.Item> = map { it.toModelItem() }
