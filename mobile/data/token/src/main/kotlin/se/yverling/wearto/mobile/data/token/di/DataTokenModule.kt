package se.yverling.wearto.mobile.data.token.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.data.token.TokenRepositoryImpl
import se.yverling.wearto.mobile.data.token.datastore.TokenDataSource
import se.yverling.wearto.mobile.data.token.datastore.TokenDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataTokenModule {
    @Binds
    @Singleton
    abstract fun bindTokenDataSource(impl: TokenDataSourceImpl): TokenDataSource

    @Binds
    @Singleton
    abstract fun bindTokenRepository(impl: TokenRepositoryImpl): TokenRepository
}
