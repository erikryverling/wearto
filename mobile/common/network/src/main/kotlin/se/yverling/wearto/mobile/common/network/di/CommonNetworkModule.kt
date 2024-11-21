package se.yverling.wearto.mobile.common.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.request.bearerAuth
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import se.yverling.wearto.mobile.common.network.exception.InvalidTokenException
import se.yverling.wearto.mobile.data.token.TokenRepository
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CommonNetworkModule {
    @Singleton
    @Provides
    @Named("todoist")
    fun provideTodoistHttpClient(tokenRepository: TokenRepository): HttpClient {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest { url(BASE_URL) }
        }

        client.plugin(HttpSend).intercept { request ->
            val token = tokenRepository.getToken().first()
            token?.let { request.bearerAuth(it) }

            val originalCall = execute(request)

            if (originalCall.response.status.value == HttpStatusCode.Unauthorized.value) {
                throw InvalidTokenException()
            }

            originalCall
        }

        return client
    }

    companion object {
        private const val BASE_URL = "https://api.todoist.com/rest/v2/"
    }
}
