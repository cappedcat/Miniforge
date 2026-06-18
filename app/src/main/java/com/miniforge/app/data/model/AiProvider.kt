package com.miniforge.app.data.model

data class AiProvider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val apiFormat: ApiFormat,
    val model: String,
    val isDefault: Boolean
)

enum class ApiFormat { OPENAI, ANTHROPIC }
