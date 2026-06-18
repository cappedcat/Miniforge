package com.miniforge.app.ai

import com.miniforge.app.data.model.ApiFormat
import com.miniforge.app.data.repository.AiProviderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class GenerationState {
    data class Streaming(val accumulated: String) : GenerationState()
    data class Complete(val html: String, val sizeKb: Int = 0) : GenerationState()
    data class Error(val message: String) : GenerationState()
}

@Singleton
class AiService @Inject constructor(
    private val providerRepository: AiProviderRepository,
    private val openAiAdapter: OpenAiAdapter,
    private val anthropicAdapter: AnthropicAdapter,
    private val htmlExtractor: HtmlExtractor
) {
    private val systemPrompt = """
        You are a mini app generator. Output a single, complete, self-contained HTML file.

        Rules:
        - ONE file only. All CSS and JavaScript must be inline (no external links).
        - No external CDN links, no external fonts, no external images.
        - Images: use CSS gradients, SVG, or base64-encoded data URIs only.
        - No eval(), no document.cookie, no localStorage for sensitive data.
        - Mobile-first. Touch-friendly. Minimum tap target 44px.
        - Semantic HTML5. Works in Android WebView (Chromium).
        - Keep output under 300KB. Hard limit: 500KB. Be concise.
        - Wrap the entire HTML in a single ```html code block.
        - Do not include explanations outside the code block.
    """.trimIndent()

    fun generate(
        userPrompt: String,
        history: List<AiMessage> = emptyList()
    ): Flow<GenerationState> = flow {
        val provider = providerRepository.getDefault()
            ?: run { emit(GenerationState.Error("No AI provider configured.")); return@flow }
        val apiKey = providerRepository.getApiKey(provider.id)
            ?: run { emit(GenerationState.Error("No API key for ${provider.name}.")); return@flow }

        val messages = history + AiMessage("user", userPrompt)
        val adapter: AiApiAdapter = when (provider.apiFormat) {
            ApiFormat.ANTHROPIC -> anthropicAdapter
            ApiFormat.OPENAI -> openAiAdapter
        }

        val sb = StringBuilder()
        runCatching {
            adapter.stream(messages, systemPrompt, apiKey, provider.model, provider.baseUrl)
                .collect { chunk ->
                    sb.append(chunk)
                    emit(GenerationState.Streaming(sb.toString()))
                }
            val html = htmlExtractor.extract(sb.toString())
                ?: run { emit(GenerationState.Error("AI did not return a valid HTML block.")); return@flow }
            val sizeKb = html.toByteArray(Charsets.UTF_8).size / 1024
            if (sizeKb > 500) {
                emit(GenerationState.Error("Generated app is too large (${sizeKb} KB, limit 500 KB). Ask for a simpler version."))
                return@flow
            }
            emit(GenerationState.Complete(html, sizeKb))
        }.onFailure { e ->
            emit(GenerationState.Error(e.message ?: "Unknown error"))
        }
    }
}
