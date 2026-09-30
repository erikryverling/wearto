package se.yverling.wearto.wear.data.items.di

import android.content.Context
import androidx.room.Room
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import se.yverling.wearto.wear.data.items.ItemStatusHolder
import se.yverling.wearto.wear.data.items.ItemStatusHolderImpl
import se.yverling.wearto.wear.data.items.ItemsRepository
import se.yverling.wearto.wear.data.items.ItemsRepositoryImpl
import se.yverling.wearto.wear.data.items.db.AppDatabase
import se.yverling.wearto.wear.data.items.sync.PlayServicesItemSyncClient
import se.yverling.wearto.wear.data.items.sync.ItemSyncClient
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
    @ApplicationScope
    internal fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    internal fun provideItemStatusHolder(
        @ApplicationScope scope: CoroutineScope,
    ): ItemStatusHolder = ItemStatusHolderImpl(scope)

    @Provides
    @Singleton
    internal fun provideDataClient(
        @ApplicationContext context: Context,
    ): DataClient = Wearable.getDataClient(context)

    @Provides
    @Singleton
    internal fun provideItemSyncClient(
        dataClient: DataClient,
    ): ItemSyncClient = PlayServicesItemSyncClient(dataClient)
}
