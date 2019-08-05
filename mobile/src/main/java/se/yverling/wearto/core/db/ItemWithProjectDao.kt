package se.yverling.wearto.core.db

import androidx.room.Dao
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Single
import se.yverling.wearto.core.entities.ItemWithProject

@Dao
interface ItemWithProjectDao {
    @Query("SELECT * FROM item INNER JOIN project ON item.project_id = project.id WHERE item.uuid = :uuid")
    fun findItemWithProjectByUuid(uuid: String): Single<ItemWithProject>

    @Query("SELECT * FROM item INNER JOIN project ON item.project_id = project.id")
    fun findAllItemsWithProject(): Single<List<ItemWithProject>>

    @Query("SELECT * FROM item INNER JOIN project ON item.project_id = project.id WHERE item.deleted = 0 ORDER BY item.name")
    fun findAllItemsWithProjectContinuously(): Flowable<List<ItemWithProject>>
}