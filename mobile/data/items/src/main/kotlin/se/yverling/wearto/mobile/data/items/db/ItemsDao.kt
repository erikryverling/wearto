package se.yverling.wearto.mobile.data.items.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ItemsDao {
    @Query("SELECT * FROM item ORDER BY name ASC")
    fun getItems(): Flow<List<Item>>

    @Query("SELECT * FROM item WHERE uid = :uid")
    fun getItem(uid: Int): Flow<Item?>

    @Upsert
    suspend fun upsertItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("DELETE FROM item")
    suspend fun deleteAllItems()
}
