package com.miniforge.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.miniforge.app.data.local.db.dao.AiProviderDao
import com.miniforge.app.data.local.db.dao.ChatMessageDao
import com.miniforge.app.data.local.db.dao.MiniAppDao
import com.miniforge.app.data.local.db.entity.AiProviderEntity
import com.miniforge.app.data.local.db.entity.ChatMessageEntity
import com.miniforge.app.data.local.db.entity.MiniAppEntity

@Database(
    entities = [MiniAppEntity::class, ChatMessageEntity::class, AiProviderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MiniForgeDatabase : RoomDatabase() {
    abstract fun miniAppDao(): MiniAppDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun aiProviderDao(): AiProviderDao
}
