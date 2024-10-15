package se.yverling.wearto.mobile.data.token.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.data.token.TokenRepositoryImpl
import se.yverling.wearto.mobile.data.token.datastore.TokenDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataTokenModule {
    @Provides
    @Singleton
    internal fun provideTokenRepository(
        dataStore: TokenDataSource,
    ): TokenRepository = TokenRepositoryImpl(dataStore)
}
