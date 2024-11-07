package se.yverling.wearto.wear.data.items.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ItemsDao {
    @Query("SELECT * FROM item ORDER BY name ASC")
    fun getItems(): Flow<List<Item>>

    @Upsert
    suspend fun setItems(items: List<Item>)
}
