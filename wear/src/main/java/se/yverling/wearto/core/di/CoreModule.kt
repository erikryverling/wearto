package se.yverling.wearto.core.di

import android.app.Application
import androidx.room.Room
import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import se.yverling.wearto.core.db.AppDatabase
import javax.inject.Singleton

internal const val WEARTO_DATABASE_NAME = "wearto-db"

@Module
class CoreModule(val app: Application) {
    @Provides
    @Singleton
    fun provideApplication(): Application  = app

    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Singleton
    fun provideWearToDatabase(): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, WEARTO_DATABASE_NAME).build()
    }

    @Provides
    @Singleton
    fun provideDataClient(context: Context): DataClient = Wearable.getDataClient(context)

    @Provides
    @Singleton
    fun provideNodeClient(context: Context): NodeClient = Wearable.getNodeClient(context)
}