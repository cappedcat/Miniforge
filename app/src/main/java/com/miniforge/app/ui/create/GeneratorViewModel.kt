package com.miniforge.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miniforge.app.ai.AiMessage
import com.miniforge.app.ai.AiService
import com.miniforge.app.ai.GenerationState
import com.miniforge.app.data.model.MiniApp
import com.miniforge.app.data.repository.MiniAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class GeneratorUiState {
    object Idle : GeneratorUiState()
    data class Streaming(val accumulated: String) : GeneratorUiState()
    data class Ready(val html: String, val sizeKb: Int = 0) : GeneratorUiState()
    data class Error(val message: String) : GeneratorUiState()
    object Saved : GeneratorUiState()
}

@HiltViewModel
class GeneratorViewModel @Inject constructor(
    private val aiService: AiService,
    private val repository: MiniAppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GeneratorUiState>(GeneratorUiState.Idle)
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    // Tracks the current app being built (set on first save / during refine)
    private var currentApp: MiniApp? = null

    // Chat history for refine sessions: list of (role, content) pairs
    // role = "user" | "assistant"
    // We store the user prompts and assistant HTML summaries so the AI has context
    private val chatHistory = mutableListOf<AiMessage>()

    // The most recent complete HTML produced (used when saving)
    private var latestHtml: String = ""

    fun generate(prompt: String, appName: String, appDescription: String) {
        _uiState.value = GeneratorUiState.Idle
        chatHistory.clear()
        currentApp = null
        latestHtml = ""

        viewModelScope.launch {
            aiService.generate(prompt).collect { state ->
                when (state) {
                    is GenerationState.Streaming -> {
                        _uiState.value = GeneratorUiState.Streaming(state.accumulated)
                    }
                    is GenerationState.Complete -> {
                        latestHtml = state.html
                        // Record the exchange in history so refine has context
                        chatHistory.add(AiMessage("user", prompt))
                        chatHistory.add(AiMessage("assistant", "[HTML app generated: ${state.sizeKb} KB]"))
                        _uiState.value = GeneratorUiState.Ready(state.html, state.sizeKb)

                        // Auto-save on first generation
                        saveApp(appName, appDescription, state.html)
                    }
                    is GenerationState.Error -> {
                        _uiState.value = GeneratorUiState.Error(state.message)
                    }
                }
            }
        }
    }

    fun refine(refinementPrompt: String) {
        val currentHtml = latestHtml
        if (currentHtml.isBlank()) return

        viewModelScope.launch {
            // Build history: existing exchanges plus a user message describing the current app
            val historyWithContext = buildList {
                addAll(chatHistory)
                add(AiMessage("user", "Here is the current app HTML:\n```html\n$currentHtml\n```\n\nNow please: $refinementPrompt"))
            }

            aiService.generate(refinementPrompt, historyWithContext).collect { state ->
                when (state) {
                    is GenerationState.Streaming -> {
                        _uiState.value = GeneratorUiState.Streaming(state.accumulated)
                    }
                    is GenerationState.Complete -> {
                        latestHtml = state.html
                        chatHistory.add(AiMessage("user", refinementPrompt))
                        chatHistory.add(AiMessage("assistant", "[HTML app updated: ${state.sizeKb} KB]"))
                        _uiState.value = GeneratorUiState.Ready(state.html, state.sizeKb)

                        // Update existing saved app
                        currentApp?.let { app ->
                            viewModelScope.launch {
                                repository.updateHtml(app.id, state.html)
                            }
                        }
                    }
                    is GenerationState.Error -> {
                        _uiState.value = GeneratorUiState.Error(state.message)
                    }
                }
            }
        }
    }

    fun saveApp(name: String, description: String, html: String = latestHtml) {
        if (html.isBlank()) return
        viewModelScope.launch {
            val app = currentApp ?: MiniApp(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "My App" },
                description = description,
                htmlFilePath = "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val saved = repository.save(app, html)
            currentApp = saved
            _uiState.value = GeneratorUiState.Saved
        }
    }

    fun reset() {
        _uiState.value = GeneratorUiState.Idle
        chatHistory.clear()
        currentApp = null
        latestHtml = ""
    }
}
