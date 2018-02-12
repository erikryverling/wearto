package se.yverling.wearto.core.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import se.yverling.wearto.core.entities.Item
import se.yverling.wearto.core.entities.Project

@Database(entities = [(Project::class), (Item::class)], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun projectDao(): ProjectDao
    abstract fun itemWithProjectDao(): ItemWithProjectDao
}