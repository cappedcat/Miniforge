package com.miniforge.app.data.model

data class MiniApp(
    val id: String,
    val name: String,
    val description: String,
    val htmlFilePath: String,
    val createdAt: Long,
    val updatedAt: Long,
    val marketplaceId: String? = null
)
