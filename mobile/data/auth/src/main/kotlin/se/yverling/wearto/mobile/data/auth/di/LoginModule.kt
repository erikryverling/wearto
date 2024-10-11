package se.yverling.wearto.mobile.data.auth.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.yverling.wearto.mobile.data.auth.AuthRepository
import se.yverling.wearto.mobile.data.auth.AuthRepositoryImpl
import se.yverling.wearto.mobile.data.auth.datastore.TokenDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoginModule {
    @Provides
    @Singleton
    internal fun provideAuthRepository(
        datasStore: TokenDataSource,
    ): AuthRepository = AuthRepositoryImpl(
        dataStore = datasStore,
    )
}
