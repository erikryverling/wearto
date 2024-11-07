package se.yverling.wearto.wear.data.items.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.yverling.wearto.wear.data.items.ItemsRepository
import se.yverling.wearto.wear.data.items.ItemsRepositoryImpl
import se.yverling.wearto.wear.data.items.db.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataItemsModule {
    @Singleton
    @Provides
    internal fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "items-database"
        ).build()
    }

    @Provides
    @Singleton
    internal fun provideItemsRepository(
        @ApplicationContext context: Context,
        db: AppDatabase,
    ): ItemsRepository = ItemsRepositoryImpl(context, db)
}
