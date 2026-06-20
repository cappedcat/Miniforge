package com.miniforge.app.di

import android.content.Context
import androidx.room.Room
import com.miniforge.app.data.local.db.MiniForgeDatabase
import com.miniforge.app.data.local.db.dao.AiProviderDao
import com.miniforge.app.data.local.db.dao.ChatMessageDao
import com.miniforge.app.data.local.db.dao.MiniAppDao
import com.miniforge.app.data.local.db.dao.ModelCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideMiniForgeDatabase(
        @ApplicationContext context: Context
    ): MiniForgeDatabase =
        Room.databaseBuilder(
            context,
            MiniForgeDatabase::class.java,
            "miniforge.db"
        ).fallbackToDestructiveMigration().build()

    @Provides fun provideMiniAppDao(db: MiniForgeDatabase): MiniAppDao = db.miniAppDao()
    @Provides fun provideChatMessageDao(db: MiniForgeDatabase): ChatMessageDao = db.chatMessageDao()
    @Provides fun provideAiProviderDao(db: MiniForgeDatabase): AiProviderDao = db.aiProviderDao()
    @Provides fun provideModelCacheDao(db: MiniForgeDatabase): ModelCacheDao = db.modelCacheDao()
}
