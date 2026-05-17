package se.yverling.wearto.mobile.data.items.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Item::class], version = 2, exportSchema = false)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun itemsDao(): ItemsDao
}
