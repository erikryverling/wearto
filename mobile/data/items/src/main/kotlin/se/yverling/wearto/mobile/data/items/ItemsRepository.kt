package se.yverling.wearto.mobile.data.items

import kotlinx.coroutines.flow.Flow
import se.yverling.wearto.mobile.data.items.model.Item

interface ItemsRepository {
    fun getItems(): Flow<List<Item>>
    suspend fun setItem(item: Item)
    suspend fun deleteItem(item: Item)
    suspend fun clearItems()
}
