package com.miniforge.app.di

import android.content.Context
import androidx.room.Room
import com.miniforge.app.data.local.db.MiniForgeDatabase
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
        ).build()
}
