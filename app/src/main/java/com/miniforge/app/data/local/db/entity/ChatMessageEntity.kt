package com.miniforge.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_history")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val appId: String,
    val role: String,   // "user" | "assistant" | "system"
    val content: String,
    val createdAt: Long
)
