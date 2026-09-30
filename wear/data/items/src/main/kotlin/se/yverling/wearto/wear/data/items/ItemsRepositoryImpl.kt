package se.yverling.wearto.wear.data.items

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.yverling.wearto.wear.data.items.db.AppDatabase
import se.yverling.wearto.wear.data.items.db.ItemsDao
import se.yverling.wearto.wear.data.items.db.toModelItems
import se.yverling.wearto.wear.data.items.model.Item
import se.yverling.wearto.wear.data.items.model.toEntities
import javax.inject.Inject

internal class ItemsRepositoryImpl internal constructor(
    private val itemsDao: ItemsDao,
) : ItemsRepository {
    @Inject
    constructor(db: AppDatabase) : this(db.itemsDao())

    override fun getItems(): Flow<List<Item>> =
        itemsDao.getItems().map { it.toModelItems() }

    override suspend fun replaceItems(items: List<Item>) {
        itemsDao.deleteAllItems()
        itemsDao.setItems(items.toEntities())
    }
}
