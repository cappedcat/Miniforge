package com.miniforge.app.data.model

data class ChatMessage(
    val id: String,
    val appId: String,
    val role: Role,
    val content: String,
    val createdAt: Long
) {
    enum class Role { USER, ASSISTANT, SYSTEM }
}
