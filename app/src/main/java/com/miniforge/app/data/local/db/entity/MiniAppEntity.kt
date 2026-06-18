package com.miniforge.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mini_apps")
data class MiniAppEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val htmlFilePath: String,
    val createdAt: Long,
    val updatedAt: Long,
    val marketplaceId: String?
)
