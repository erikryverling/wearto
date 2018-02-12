package se.yverling.wearto.core.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import se.yverling.wearto.core.entities.Item

@Dao
interface ItemDao {
    @Query("SELECT * FROM item ORDER BY name")
    fun findAll(): Flowable<List<Item>>

    @Insert
    fun saveAll(project: List<Item>)

    @Query("DELETE FROM item")
    fun deleteAll()
}