package com.miniforge.app.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface AiApiAdapter {
    fun stream(
        messages: List<AiMessage>,
        systemPrompt: String,
        apiKey: String,
        model: String,
        baseUrl: String
    ): Flow<String>
}

@Serializable
data class AiMessage(val role: String, val content: String)
