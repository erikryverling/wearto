package se.yverling.wearto.core.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project")
data class Project(@PrimaryKey val id: Long, @ColumnInfo(name = "project_name") val name: String, val color: Int)