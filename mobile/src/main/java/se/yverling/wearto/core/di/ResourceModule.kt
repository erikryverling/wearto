package se.yverling.wearto.core.di

import com.facebook.stetho.okhttp3.StethoInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import se.yverling.wearto.core.API_BASE_URL
import se.yverling.wearto.sync.network.CompletedResource
import se.yverling.wearto.sync.network.ProjectsResource
import se.yverling.wearto.sync.network.SyncResource
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ResourceModule {
    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .addNetworkInterceptor(StethoInterceptor())
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
    }

    @Singleton
    @Provides
    fun provideSyncResource(client: OkHttpClient): SyncResource {
        return Retrofit.Builder()
                .client(client)
                .baseUrl(API_BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(SyncResource::class.java)
    }

    @Singleton
    @Provides
    fun provideProjectsResource(client: OkHttpClient): ProjectsResource {
        return Retrofit.Builder()
                .client(client)
                .baseUrl(API_BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(ProjectsResource::class.java)
    }

    @Singleton
    @Provides
    fun provideCompletedResource(client: OkHttpClient): CompletedResource {
        return Retrofit.Builder()
                .client(client)
                .baseUrl(API_BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(CompletedResource::class.java)
    }
}
