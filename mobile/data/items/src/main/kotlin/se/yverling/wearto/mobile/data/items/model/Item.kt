package se.yverling.wearto.mobile.data.items.model

data class Item(val name: String)

internal fun List<Item>.toDbItems(): List<se.yverling.wearto.mobile.data.items.db.Item> =
    this.map { item -> se.yverling.wearto.mobile.data.items.db.Item(name = item.name) }
