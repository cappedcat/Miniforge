package com.miniforge.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_providers")
data class AiProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val baseUrl: String,
    val apiFormat: String,  // "openai" | "anthropic"
    val model: String,
    val isDefault: Boolean
)
