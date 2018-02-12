package se.yverling.wearto.core.di

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import se.yverling.wearto.core.db.AppDatabase
import javax.inject.Singleton

internal const val SHARED_PREFERENCES_FILE_NAME = "WEARTO"
internal const val WEARTO_DATABASE_NAME = "wearto-db"

@Module
open class CoreModule(private val app: Application) {
    @Provides
    @Singleton
    fun provideApplication(): Application = app

    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideWearToDatabase(): AppDatabase = Room.databaseBuilder(app, AppDatabase::class.java, WEARTO_DATABASE_NAME).build()

    @Provides
    @Singleton
    open fun provideDataClient(context: Context): DataClient = Wearable.getDataClient(context)

    @Provides
    @Singleton
    open fun provideNoteClient(context: Context): NodeClient = Wearable.getNodeClient(context)
}