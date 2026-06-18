package com.miniforge.app.di

import com.miniforge.app.data.local.db.MiniForgeDatabase
import com.miniforge.app.data.repository.AiProviderRepository
import com.miniforge.app.data.repository.ChatMessageRepository
import com.miniforge.app.data.repository.MiniAppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideMiniAppRepository(database: MiniForgeDatabase): MiniAppRepository =
        MiniAppRepository(database.miniAppDao())

    @Singleton
    @Provides
    fun provideChatMessageRepository(database: MiniForgeDatabase): ChatMessageRepository =
        ChatMessageRepository(database.chatMessageDao())

    @Singleton
    @Provides
    fun provideAiProviderRepository(database: MiniForgeDatabase): AiProviderRepository =
        AiProviderRepository(database.aiProviderDao())
}
