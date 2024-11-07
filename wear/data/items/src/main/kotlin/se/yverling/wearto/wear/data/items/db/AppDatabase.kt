package se.yverling.wearto.wear.data.items.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Item::class], version = 1, exportSchema = false)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun itemsDao(): ItemsDao
}
