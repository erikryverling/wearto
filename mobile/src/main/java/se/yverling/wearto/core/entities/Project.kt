package se.yverling.wearto.core.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "project")
data class Project(@PrimaryKey val id: Long, @ColumnInfo(name = "project_name") val name: String, val color: Int)