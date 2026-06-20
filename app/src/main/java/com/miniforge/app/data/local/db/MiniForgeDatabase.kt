package com.miniforge.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.miniforge.app.data.local.db.dao.AiProviderDao
import com.miniforge.app.data.local.db.dao.ChatMessageDao
import com.miniforge.app.data.local.db.dao.MiniAppDao
import com.miniforge.app.data.local.db.dao.ModelCacheDao
import com.miniforge.app.data.local.db.entity.AiProviderEntity
import com.miniforge.app.data.local.db.entity.ChatMessageEntity
import com.miniforge.app.data.local.db.entity.MiniAppEntity
import com.miniforge.app.data.local.db.entity.ModelCacheEntity

@Database(
    entities = [MiniAppEntity::class, ChatMessageEntity::class, AiProviderEntity::class, ModelCacheEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MiniForgeDatabase : RoomDatabase() {
    abstract fun miniAppDao(): MiniAppDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun aiProviderDao(): AiProviderDao
    abstract fun modelCacheDao(): ModelCacheDao
}
