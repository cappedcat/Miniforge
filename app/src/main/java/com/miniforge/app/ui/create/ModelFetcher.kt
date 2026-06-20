package com.miniforge.app.ui.create

import android.util.Log
import com.miniforge.app.data.local.db.dao.ModelCacheDao
import com.miniforge.app.data.local.db.entity.ModelCacheEntity
import com.miniforge.app.data.model.AiProvider
import com.miniforge.app.data.repository.AiProviderRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ModelFetcher(
    private val httpClient: HttpClient,
    private val modelCacheDao: ModelCacheDao,
    private val providerRepository: AiProviderRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchAndCacheModels(provider: AiProvider): List<String> = withContext(Dispatchers.IO) {
        // Check cache first — valid for 24 hours
        val cached = modelCacheDao.getModelsSuspend(provider.id)
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        if (cached.isNotEmpty() && (now - cached.first().lastUpdated) < oneDayMs) {
            return@withContext cached.map { it.modelId }
        }

        val apiKey = try {
            providerRepository.getApiKey(provider.id) ?: return@withContext fallbackModels(provider)
        } catch (e: Exception) {
            Log.w("ModelFetcher", "Failed to get API key for ${provider.name}")
            return@withContext fallbackModels(provider)
        }

        val fetched = try {
            when {
                provider.baseUrl.contains("anthropic") -> fetchAnthropicModels(provider, apiKey)
                provider.baseUrl.contains("openai.com") -> fetchOpenAiModels(provider, apiKey)
                provider.baseUrl.contains("groq") -> fetchGroqModels(provider, apiKey)
                provider.baseUrl.contains("mistral") -> fetchMistralModels(provider, apiKey)
                provider.baseUrl.contains("openrouter") -> fetchOpenRouterModels(provider, apiKey)
                provider.baseUrl.contains("google") || provider.baseUrl.contains("gemini") || provider.baseUrl.contains("generativelanguage") -> fetchGeminiModels(provider, apiKey)
                else -> emptyList()
            }
        } catch (e: Exception) {
            Log.w("ModelFetcher", "API fetch failed for ${provider.name}: ${e.message}")
            emptyList()
        }

        val models = fetched.ifEmpty {
            Log.d("ModelFetcher", "Using fallback models for ${provider.name}")
            fallbackModels(provider)
        }

        // Cache the result
        if (models.isNotEmpty()) {
            try {
                val entities = models.map {
                    ModelCacheEntity(
                        id = "${provider.id}_$it",
                        providerId = provider.id,
                        modelId = it,
                        modelName = it,
                        contextWindow = 0,
                        lastUpdated = now
                    )
                }
                modelCacheDao.deleteAllForProvider(provider.id)
                modelCacheDao.insertModels(entities)
            } catch (e: Exception) {
                Log.e("ModelFetcher", "Failed to cache models", e)
            }
        }

        return@withContext models
    }

    private fun fallbackModels(provider: AiProvider): List<String> = when {
        provider.baseUrl.contains("anthropic") -> listOf(
            "claude-opus-4-8",
            "claude-sonnet-4-6",
            "claude-haiku-4-5-20251001",
            "claude-3-7-sonnet-20250219",
            "claude-3-5-haiku-20241022"
        )
        provider.baseUrl.contains("openai.com") -> listOf(
            "gpt-4o",
            "gpt-4o-mini",
            "gpt-4-turbo",
            "gpt-4",
            "gpt-3.5-turbo",
            "o1",
            "o1-mini",
            "o3-mini"
        )
        provider.baseUrl.contains("groq") -> listOf(
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant",
            "llama3-70b-8192",
            "llama3-8b-8192",
            "deepseek-r1-distill-llama-70b",
            "gemma2-9b-it",
            "qwen-qwq-32b",
            "mixtral-8x7b-32768"
        )
        provider.baseUrl.contains("mistral") -> listOf(
            "mistral-large-latest",
            "mistral-medium-latest",
            "mistral-small-latest",
            "codestral-latest",
            "open-mixtral-8x22b",
            "open-mistral-nemo"
        )
        provider.baseUrl.contains("openrouter") -> listOf(
            "openai/gpt-4o",
            "openai/gpt-4o-mini",
            "anthropic/claude-sonnet-4-6",
            "anthropic/claude-3-5-haiku",
            "google/gemini-2.0-flash-exp",
            "meta-llama/llama-3.3-70b-instruct",
            "deepseek/deepseek-r1",
            "mistralai/mistral-large",
            "qwen/qwen-2.5-72b-instruct",
            "microsoft/phi-4"
        )
        provider.baseUrl.contains("google") || provider.baseUrl.contains("gemini") || provider.baseUrl.contains("generativelanguage") -> listOf(
            "gemini-2.0-flash",
            "gemini-2.0-flash-lite",
            "gemini-1.5-pro",
            "gemini-1.5-flash",
            "gemini-1.5-flash-8b"
        )
        else -> listOf(provider.model)
    }.filter { it.isNotEmpty() }

    private suspend fun fetchAnthropicModels(provider: AiProvider, apiKey: String): List<String> {
        val response = httpClient.get("${provider.baseUrl}/v1/models") {
            headers.append("x-api-key", apiKey)
            headers.append("anthropic-version", "2023-06-01")
        }.bodyAsText()
        val root = json.parseToJsonElement(response).jsonObject
        return root["data"]?.jsonArray
            ?.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.content }
            ?.filter { it.startsWith("claude") }
            ?.sortedDescending()
            ?: emptyList()
    }

    private suspend fun fetchOpenAiModels(provider: AiProvider, apiKey: String): List<String> {
        val response = httpClient.get("${provider.baseUrl}/v1/models") {
            headers.append("Authorization", "Bearer $apiKey")
        }.bodyAsText()
        val root = json.parseToJsonElement(response).jsonObject
        return root["data"]?.jsonArray
            ?.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.content }
            ?.filter { id -> listOf("gpt-", "o1", "o3", "o4").any { id.startsWith(it) } }
            ?.sortedDescending()
            ?: emptyList()
    }

    private suspend fun fetchGroqModels(provider: AiProvider, apiKey: String): List<String> {
        // Groq uses OpenAI-compatible endpoint
        val baseUrl = provider.baseUrl.trimEnd('/')
        val url = if (baseUrl.endsWith("/openai")) "$baseUrl/v1/models" else "$baseUrl/openai/v1/models"
        val response = httpClient.get(url) {
            headers.append("Authorization", "Bearer $apiKey")
        }.bodyAsText()
        val root = json.parseToJsonElement(response).jsonObject
        return root["data"]?.jsonArray
            ?.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.content }
            ?.filter { !it.contains("whisper") && !it.contains("tts") }
            ?.sorted()
            ?: emptyList()
    }

    private suspend fun fetchMistralModels(provider: AiProvider, apiKey: String): List<String> {
        val response = httpClient.get("${provider.baseUrl}/v1/models") {
            headers.append("Authorization", "Bearer $apiKey")
        }.bodyAsText()
        val root = json.parseToJsonElement(response).jsonObject
        return root["data"]?.jsonArray
            ?.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.content }
            ?.filter { !it.contains("embed") }
            ?.sorted()
            ?: emptyList()
    }

    private suspend fun fetchOpenRouterModels(provider: AiProvider, apiKey: String): List<String> {
        val response = httpClient.get("https://openrouter.ai/api/v1/models") {
            headers.append("Authorization", "Bearer $apiKey")
        }.bodyAsText()
        val root = json.parseToJsonElement(response).jsonObject
        return root["data"]?.jsonArray
            ?.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.content }
            ?.sorted()
            ?: emptyList()
    }

    private suspend fun fetchGeminiModels(provider: AiProvider, apiKey: String): List<String> {
        val response = httpClient.get("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
            .bodyAsText()
        val root = json.parseToJsonElement(response).jsonObject
        return root["models"]?.jsonArray
            ?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.content?.removePrefix("models/") }
            ?.filter { it.startsWith("gemini") && !it.contains("embedding") && !it.contains("aqa") }
            ?.sorted()
            ?: emptyList()
    }
}
