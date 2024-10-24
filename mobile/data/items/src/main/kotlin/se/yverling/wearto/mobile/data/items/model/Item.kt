package se.yverling.wearto.mobile.data.items.model

data class Item(val uid: Int? = null, val name: String)

internal fun Item.toEntity() = se.yverling.wearto.mobile.data.items.db.Item(uid = uid, name = name)
internal fun List<Item>.toEntities(): List<se.yverling.wearto.mobile.data.items.db.Item> = map { it.toEntity() }
