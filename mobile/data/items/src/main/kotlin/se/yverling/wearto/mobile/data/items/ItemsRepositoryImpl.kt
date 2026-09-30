package se.yverling.wearto.mobile.data.items

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.yverling.wearto.mobile.data.items.db.AppDatabase
import se.yverling.wearto.mobile.data.items.db.ItemsDao
import se.yverling.wearto.mobile.data.items.db.toModels
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.data.items.model.toEntities
import se.yverling.wearto.mobile.data.items.model.toEntity
import javax.inject.Inject

internal class ItemsRepositoryImpl internal constructor(
    private val itemsDao: ItemsDao,
) : ItemsRepository {
    @Inject
    constructor(db: AppDatabase) : this(db.itemsDao())

    override fun getItems(): Flow<List<Item>> =
        itemsDao.getItems().map { it.toModels() }

    override suspend fun setItem(item: Item) {
        itemsDao.upsertItem(item.toEntity())
    }

    override suspend fun setItems(items: List<Item>) {
        itemsDao.insertItems(items.toEntities())
    }

    override suspend fun deleteItem(item: Item) {
        itemsDao.deleteItem(item.toEntity())
    }

    override suspend fun clearItems() {
        itemsDao.deleteAllItems()
    }
}
