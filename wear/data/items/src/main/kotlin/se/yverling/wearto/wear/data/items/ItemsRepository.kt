package se.yverling.wearto.wear.data.items

import kotlinx.coroutines.flow.Flow
import se.yverling.wearto.wear.data.items.model.Item

interface ItemsRepository {
    fun getItems(): Flow<List<Item>>
    suspend fun replaceItems(items: List<Item>)
}
