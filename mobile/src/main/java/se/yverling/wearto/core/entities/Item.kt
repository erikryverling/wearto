package se.yverling.wearto.core.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item")
data class Item(
        @PrimaryKey val uuid: String,
        val name: String,
        @ColumnInfo(name = "project_id") val projectId: Long,
        val deleted: Boolean = false)