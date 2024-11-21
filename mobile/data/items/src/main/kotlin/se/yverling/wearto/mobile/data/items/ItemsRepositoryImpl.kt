package se.yverling.wearto.mobile.data.items

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.yverling.wearto.mobile.data.items.db.AppDatabase
import se.yverling.wearto.mobile.data.items.db.toModels
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.data.items.model.toEntity
import javax.inject.Inject

internal class ItemsRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
) : ItemsRepository {
    override fun getItems(): Flow<List<Item>> =
        db.itemsDao().getItems().map { it.toModels() }

    override suspend fun setItem(item: Item) {
        db.itemsDao().upsertItem(item.toEntity())
    }

    override suspend fun deleteItem(item: Item) {
        db.itemsDao().deleteItem(item.toEntity())
    }

    override suspend fun clearItems() {
        db.itemsDao().deleteAllItems()
    }
}
