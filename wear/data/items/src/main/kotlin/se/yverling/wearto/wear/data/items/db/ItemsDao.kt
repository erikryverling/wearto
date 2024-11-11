package se.yverling.wearto.wear.data.items.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import se.yverling.wearto.wear.data.items.model.ItemState

@Dao
internal interface ItemsDao {
    @Query("SELECT * FROM item ORDER BY name ASC")
    fun getItems(): Flow<List<Item>>

    @Insert
    suspend fun setItems(items: List<Item>)

    @Query("UPDATE item SET state = :stateName")
    suspend fun resetAllItemState(stateName: String = ItemState.Init.name)

    @Query("DELETE FROM item")
    suspend fun deleteAllItems()

    @Query("SELECT * FROM item WHERE name = :name")
    fun getItemByName(name: String): Flow<Item>

    @Upsert
    suspend fun setItem(item: Item)
}
