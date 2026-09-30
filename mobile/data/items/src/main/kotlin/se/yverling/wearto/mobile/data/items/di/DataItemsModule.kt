package se.yverling.wearto.mobile.data.items.di

import android.content.Context
import androidx.room.Room
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.yverling.wearto.mobile.data.items.ItemsRepository
import se.yverling.wearto.mobile.data.items.ItemsRepositoryImpl
import se.yverling.wearto.mobile.data.items.db.AppDatabase
import se.yverling.wearto.mobile.data.items.sync.PlayServicesItemsSyncPublisher
import se.yverling.wearto.mobile.data.items.sync.ItemsSyncPublisher
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
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    internal fun provideItemsRepository(db: AppDatabase): ItemsRepository =
        ItemsRepositoryImpl(db)

    @Provides
    @Singleton
    internal fun provideDataClient(
        @ApplicationContext context: Context,
    ): DataClient = Wearable.getDataClient(context)

    @Provides
    @Singleton
    internal fun provideItemsSyncPublisher(
        dataClient: DataClient,
    ): ItemsSyncPublisher = PlayServicesItemsSyncPublisher(dataClient)
}
