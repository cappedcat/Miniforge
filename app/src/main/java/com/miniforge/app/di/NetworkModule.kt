package com.miniforge.app.di

import com.miniforge.app.data.local.db.dao.ModelCacheDao
import com.miniforge.app.data.repository.AiProviderRepository
import com.miniforge.app.ui.create.ModelFetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideJson(): Json =
        Json {
            prettyPrint = false
            ignoreUnknownKeys = true
        }

    @Singleton
    @Provides
    fun provideHttpClient(json: Json): HttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 120_000   // 2 min between SSE chunks
                requestTimeoutMillis = 300_000  // 5 min total (large HTML generation)
            }
        }

    @Singleton
    @Provides
    fun provideModelFetcher(httpClient: HttpClient, modelCacheDao: ModelCacheDao, providerRepository: AiProviderRepository): ModelFetcher =
        ModelFetcher(httpClient, modelCacheDao, providerRepository)
}
