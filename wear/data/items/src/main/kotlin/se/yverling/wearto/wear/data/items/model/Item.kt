package se.yverling.wearto.wear.data.items.model

import se.yverling.wearto.wear.data.items.db.Item as ItemEntity

data class Item(
    val uid: Int? = null,
    val name: String,
    val state: ItemState = ItemState.Init,
)

private fun Item.toEntity() = ItemEntity(uid = uid, name = name)
internal fun List<Item>.toEntities(): List<ItemEntity> = map { it.toEntity() }
