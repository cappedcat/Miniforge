package com.miniforge.app.data.model

data class MiniApp(
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val aiProvider: AiProvider
)
