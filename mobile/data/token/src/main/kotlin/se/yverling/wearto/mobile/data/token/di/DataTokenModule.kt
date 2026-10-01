package se.yverling.wearto.mobile.data.token.di

import com.google.crypto.tink.Aead
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.yverling.wearto.mobile.data.token.TokenRepository
import se.yverling.wearto.mobile.data.token.TokenRepositoryImpl
import se.yverling.wearto.mobile.data.token.crypto.TokenAead
import se.yverling.wearto.mobile.data.token.crypto.TokenAeadManager
import se.yverling.wearto.mobile.data.token.datastore.LegacyTokenDataSource
import se.yverling.wearto.mobile.data.token.datastore.LegacyTokenDataSourceImpl
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
    abstract fun bindLegacyTokenDataSource(impl: LegacyTokenDataSourceImpl): LegacyTokenDataSource

    @Binds
    @Singleton
    abstract fun bindTokenRepository(impl: TokenRepositoryImpl): TokenRepository

    companion object {
        @Provides
        @Singleton
        @TokenAead
        fun provideTokenAead(tokenAeadManager: TokenAeadManager): Aead = tokenAeadManager.aead
    }
}
