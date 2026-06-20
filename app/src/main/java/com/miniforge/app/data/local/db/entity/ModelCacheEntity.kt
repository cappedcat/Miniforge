package com.miniforge.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "model_cache")
data class ModelCacheEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val modelId: String,
    val modelName: String,
    val contextWindow: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
