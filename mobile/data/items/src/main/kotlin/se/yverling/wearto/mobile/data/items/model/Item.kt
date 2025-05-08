package se.yverling.wearto.mobile.data.items.model

import se.yverling.wearto.mobile.data.items.db.Item as EntityItem

data class Item(val uid: Int? = null, val name: String)

internal fun Item.toEntity() = EntityItem(uid = uid, name = name)

internal fun List<Item>.toEntities() = this.map { it.toEntity() }
