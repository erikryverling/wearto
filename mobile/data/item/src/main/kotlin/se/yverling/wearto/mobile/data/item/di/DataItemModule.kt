package se.yverling.wearto.mobile.data.item.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.yverling.wearto.mobile.data.item.ItemRepository
import se.yverling.wearto.mobile.data.item.ItemRepositoryImpl
import se.yverling.wearto.mobile.data.item.network.TasksEndpoint
import se.yverling.wearto.mobile.data.settings.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataItemModule {
    @Provides
    @Singleton
    internal fun provideSettingsRepository(
        tasksEndpoint: TasksEndpoint,
        settingsRepository: SettingsRepository,
    ): ItemRepository = ItemRepositoryImpl(tasksEndpoint, settingsRepository)
}
