package se.yverling.wearto.wear.data.items.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import se.yverling.wearto.wear.data.items.model.ItemState

import se.yverling.wearto.wear.data.items.model.Item as ItemModel

@Entity(indices = [Index(value = ["name"], unique = true)])
@TypeConverters(Converters::class)
internal data class Item(
    @PrimaryKey(autoGenerate = true)
    val uid: Int? = null,
    val name: String,
    val state: ItemState = ItemState.Init,
)

internal class Converters {
    @TypeConverter
    fun toState(value: String) = enumValueOf<ItemState>(value)

    @TypeConverter
    fun fromState(value: ItemState) = value.name
}

private fun Item.toModelItem() = ItemModel(uid = uid, name = name, state = state)
internal fun List<Item>.toModelItems(): List<ItemModel> = map { it.toModelItem() }
