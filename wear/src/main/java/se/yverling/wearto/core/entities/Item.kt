package se.yverling.wearto.core.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "item")
class Item(
        @PrimaryKey val uuid: String,
        val name: String,
        @ColumnInfo(name = "project_name") val projectName: String,
        val color: Int)