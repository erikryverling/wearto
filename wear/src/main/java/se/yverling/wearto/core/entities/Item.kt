package se.yverling.wearto.core.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item")
class Item(
        @PrimaryKey val uuid: String,
        val name: String,
        @ColumnInfo(name = "project_name") val projectName: String,
        val color: Int)