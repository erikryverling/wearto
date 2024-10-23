package se.yverling.wearto.mobile.data.items

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.yverling.wearto.mobile.data.items.db.AppDatabase
import se.yverling.wearto.mobile.data.items.db.toModelItems
import se.yverling.wearto.mobile.data.items.model.Item
import se.yverling.wearto.mobile.data.items.model.toDbItems
import javax.inject.Inject

internal class ItemsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
) : ItemsRepository {
    override fun getItems(): Flow<List<Item>> =
        db.itemsDao().getItems().map { it.toModelItems() }

    override suspend fun setItems(items: List<Item>) {
        db.itemsDao().setItems(items.toDbItems())
    }
}
