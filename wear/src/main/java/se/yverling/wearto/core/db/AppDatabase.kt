package se.yverling.wearto.core.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import se.yverling.wearto.core.entities.Item

@Database(entities = [(Item::class)], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}