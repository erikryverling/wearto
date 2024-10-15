package se.yverling.wearto.mobile.data.settings.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.yverling.wearto.mobile.data.settings.SettingsRepository
import se.yverling.wearto.mobile.data.settings.SettingsRepositoryImpl
import se.yverling.wearto.mobile.data.settings.datastore.ProjectDataStore
import se.yverling.wearto.mobile.data.settings.network.ProjectsEndpoint
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataSettingsModule {
    @Provides
    @Singleton
    internal fun provideSettingsRepository(
        projectsEndpoint: ProjectsEndpoint,
        projectDataStore: ProjectDataStore,
    ): SettingsRepository = SettingsRepositoryImpl(projectsEndpoint, projectDataStore)
}
