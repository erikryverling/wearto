package se.yverling.wearto.wear.feature.items.model

import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.data.items.model.ItemState

data class ItemUiModel(
    val item: Item,
    val state: ItemState = ItemState.Init,
)
