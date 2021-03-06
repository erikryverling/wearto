package se.yverling.wearto.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Maybe
import io.reactivex.Single
import se.yverling.wearto.core.entities.Item

@Dao
interface ItemDao {
    @Query("SELECT * FROM item WHERE uuid = :uuid")
    fun findByUuid(uuid: String): Single<Item>

    @Query("SELECT * FROM item WHERE name = :name AND project_id = :projectId")
    fun findByNameAndProjectId(name: String, projectId: Long): Maybe<Item>

    @Query("SELECT * FROM item WHERE project_id NOT IN (SELECT id FROM project)")
    fun findAllOrphans(): Single<List<Item>>

    @Insert
    fun save(item: Item)

    @Update
    fun update(item: Item)

    @Query("DELETE FROM item")
    fun deleteAll()
}