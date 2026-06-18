package com.miniforge.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
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
        }
}
