package com.miniforge.app.ai

import android.util.Log
import com.miniforge.app.data.model.ApiFormat
import com.miniforge.app.data.repository.AiProviderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class GenerationState {
    data class Streaming(val accumulated: String) : GenerationState()
    data class Complete(val html: String) : GenerationState()
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
        You are a mini app generator. Your ENTIRE response must be one fenced code block and nothing else.

        REQUIRED OUTPUT FORMAT — follow exactly:
        ```html
        <!DOCTYPE html>
        <html>
        ...complete app here...
        </html>
        ```

        Do NOT write any text before or after the code block. No introduction, no explanation, no comments outside the block.

        Rules for the HTML:
        - ONE file. All CSS and JavaScript must be inline — no external links, no CDN, no imports.
        - No external images. Use CSS gradients, SVG inline, or base64 data URIs.
        - No eval(), no document.cookie access, no sensitive localStorage usage.
        - Mobile-first layout. Touch-friendly tap targets (minimum 44px height).
        - Works in Android WebView (Chromium-based). Use standard HTML5/CSS3/ES6.
    """.trimIndent()

    fun generate(
        userPrompt: String,
        history: List<AiMessage> = emptyList(),
        providerId: String? = null,
        modelId: String? = null
    ): Flow<GenerationState> = flow {
        val provider = if (providerId != null)
            providerRepository.getById(providerId)
        else
            providerRepository.getDefault()
        provider ?: run { emit(GenerationState.Error("No AI provider configured. Add one in Settings → AI Providers.")); return@flow }

        val apiKey = providerRepository.getApiKey(provider.id)
            ?: run { emit(GenerationState.Error("No API key for ${provider.name}. Check Settings → AI Providers.")); return@flow }

        // Use selected model if provided, otherwise use provider's default
        val model = modelId ?: provider.model

        val messages = history + AiMessage("user", userPrompt)
        val adapter: AiApiAdapter = when (provider.apiFormat) {
            ApiFormat.ANTHROPIC -> anthropicAdapter
            ApiFormat.OPENAI -> openAiAdapter
        }

        val sb = StringBuilder()
        runCatching {
            adapter.stream(messages, systemPrompt, apiKey, model, provider.baseUrl)
                .collect { chunk ->
                    sb.append(chunk)
                    emit(GenerationState.Streaming(sb.toString()))
                }
            val raw = sb.toString()
            Log.d("AiService", "Response length: ${raw.length} chars")
            Log.d("AiService", "START[0-200]: ${raw.take(200)}")
            Log.d("AiService", "START[200-400]: ${raw.substring(200, minOf(400, raw.length))}")
            Log.d("AiService", "MID: ${raw.substring(maxOf(0, raw.length/2 - 100), minOf(raw.length, raw.length/2 + 100))}")
            Log.d("AiService", "END: ${raw.takeLast(200)}")
            if (raw.isEmpty()) {
                emit(GenerationState.Error("API returned no data. Check your API key and rate limits in Settings → AI Providers."))
                return@flow
            }
            val html = htmlExtractor.extract(raw)
                ?: run {
                    // Fallback: try to locate <html>...</html> block directly
                    val regex = Regex("<html[\\s\\S]*?</html>", RegexOption.IGNORE_CASE)
                    val match = regex.find(raw)
                    if (match != null) {
                        Log.d("AiService", "Fallback HTML extraction succeeded via regex.")
                        match.value
                    } else {
                        Log.d("AiService", "HTML extraction failed. Response contains: ${raw.contains("```")}, ${raw.contains("<html")}, ${raw.contains("<!DOCTYPE")}")
                        emit(GenerationState.Error("AI did not return a valid HTML block. Try rephrasing your prompt."))
                        return@flow
                    }
                }
                
            emit(GenerationState.Complete(html))
        }.onFailure { e ->
            if (e is CancellationException) throw e
            emit(GenerationState.Error(e.message ?: "Unknown error during generation"))
        }
    }
}
