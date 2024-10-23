package se.yverling.wearto.mobile.data.items.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ItemsDao {
    @Query("SELECT * FROM item")
    fun getItems(): Flow<List<Item>>

    @Insert
    suspend fun setItems(items: List<Item>)
}
