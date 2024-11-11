package se.yverling.wearto.wear.data.items

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import se.yverling.wearto.wear.data.items.db.AppDatabase
import se.yverling.wearto.wear.data.items.db.toModelItems
import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.data.items.model.ItemState
import se.yverling.wearto.wear.data.items.model.toEntities
import javax.inject.Inject

internal class ItemsRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
) : ItemsRepository {
    override fun getItems(): Flow<List<Item>> =
        db.itemsDao().getItems().map { it.toModelItems() }

    override suspend fun replaceItems(items: List<Item>) {
        db.itemsDao().deleteAllItems()
        db.itemsDao().setItems(items.toEntities())
    }

    override suspend fun resetAllItemState() {
        db.itemsDao().resetAllItemState()
    }

    override suspend fun updateItemState(name: String, state: ItemState) {
        val item = db.itemsDao().getItemByName(name).firstOrNull()
        item?.let { db.itemsDao().setItem(item.copy(state = state)) }
    }
}
