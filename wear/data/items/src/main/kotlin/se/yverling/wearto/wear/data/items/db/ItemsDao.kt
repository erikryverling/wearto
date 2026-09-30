package se.yverling.wearto.wear.data.items.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ItemsDao {
    @Query("SELECT * FROM item ORDER BY name ASC")
    fun getItems(): Flow<List<Item>>

    @Insert
    suspend fun setItems(items: List<Item>)

    @Query("DELETE FROM item")
    suspend fun deleteAllItems()
}
