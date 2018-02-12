package se.yverling.wearto.core.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Single
import se.yverling.wearto.core.entities.Project

@Dao
interface ProjectDao {
    @Query("SELECT * FROM project WHERE project_name = :name")
    fun findByName(name: String): Single<Project>

    @Query("SELECT * FROM project ORDER BY project_name")
    fun findAll(): Single<List<Project>>

    @Insert
    fun saveAll(project: List<Project>)

    @Query("DELETE FROM project")
    fun deleteAll()
}