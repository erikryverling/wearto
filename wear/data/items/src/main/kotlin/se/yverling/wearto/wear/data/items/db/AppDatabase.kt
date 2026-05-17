package se.yverling.wearto.wear.data.items.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Item::class], version = 2, exportSchema = false)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun itemsDao(): ItemsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Item ADD COLUMN interactionCount INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
