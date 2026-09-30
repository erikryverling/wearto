package se.yverling.wearto.mobile.data.items.sync

import se.yverling.wearto.mobile.data.items.model.Item

interface ItemsSyncPublisher {
    suspend fun publishItems(items: List<Item>): Result<Unit>
}
