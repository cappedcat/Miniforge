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
    private val json = Json

    suspend fun fetchAndCacheModels(provider: AiProvider): List<String> = withContext(Dispatchers.IO) {
        // Check cache first - use if less than 24 hours old
        val cached = modelCacheDao.getModelsSuspend(provider.id)
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        if (cached.isNotEmpty() && (now - cached.first().lastUpdated) < oneDayMs) {
            return@withContext cached.map { it.modelId }
        }

        // Get API key from repository
        val apiKey = try {
            providerRepository.getApiKey(provider.id) ?: return@withContext emptyList()
        } catch (e: Exception) {
            Log.w("ModelFetcher", "Failed to get API key for ${provider.name}", e)
            return@withContext emptyList()
        }

        // Fetch fresh models from provider API
        val models = try {
            when {
                provider.baseUrl.contains("anthropic") -> fetchAnthropicModels(provider, apiKey)
                provider.baseUrl.contains("openai") -> fetchOpenAiModels(provider, apiKey)
                provider.baseUrl.contains("groq") -> fetchGroqModels(provider, apiKey)
                provider.baseUrl.contains("mistral") -> fetchMistralModels(provider, apiKey)
                provider.baseUrl.contains("openrouter") -> fetchOpenRouterModels(provider, apiKey)
                provider.baseUrl.contains("google") -> fetchGeminiModels(provider, apiKey)
                else -> emptyList()
            }
        } catch (e: Exception) {
            Log.w("ModelFetcher", "Failed to fetch models for ${provider.name}", e)
            emptyList()
        }

        // Cache new models
        if (models.isNotEmpty()) {
            val entities = models.map { ModelCacheEntity(
                id = "${provider.id}_$it",
                providerId = provider.id,
                modelId = it,
                modelName = it,
                contextWindow = 0,
                lastUpdated = now
            ) }
            try {
                modelCacheDao.deleteAllForProvider(provider.id)
                modelCacheDao.insertModels(entities)
            } catch (e: Exception) {
                Log.e("ModelFetcher", "Failed to cache models", e)
            }
        }

        return@withContext models
    }

    private suspend fun fetchAnthropicModels(provider: AiProvider, apiKey: String): List<String> {
        val url = "${provider.baseUrl}/v1/models"
        val response = httpClient.get(url) {
            headers.append("x-api-key", apiKey)
        }.bodyAsText()

        val json = json.parseToJsonElement(response).jsonObject
        val models = mutableListOf<String>()
        json["data"]?.jsonArray?.forEach { model ->
            val id = model.jsonObject["id"]?.jsonPrimitive?.content
            if (id != null && id.startsWith("claude")) {
                models.add(id)
            }
        }
        return models.sorted()
    }

    private suspend fun fetchOpenAiModels(provider: AiProvider, apiKey: String): List<String> {
        val url = "${provider.baseUrl}/v1/models"
        val response = httpClient.get(url) {
            headers.append("Authorization", "Bearer $apiKey")
        }.bodyAsText()

        val json = json.parseToJsonElement(response).jsonObject
        val models = mutableListOf<String>()
        json["data"]?.jsonArray?.forEach { model ->
            val id = model.jsonObject["id"]?.jsonPrimitive?.content
            if (id != null && (id.startsWith("gpt") || id.startsWith("text-"))) {
                models.add(id)
            }
        }
        return models.sorted()
    }

    private suspend fun fetchGroqModels(provider: AiProvider, apiKey: String): List<String> {
        val url = "${provider.baseUrl}/openai/v1/models"
        val response = httpClient.get(url) {
            headers.append("Authorization", "Bearer $apiKey")
        }.bodyAsText()

        val json = json.parseToJsonElement(response).jsonObject
        val models = mutableListOf<String>()
        json["data"]?.jsonArray?.forEach { model ->
            val id = model.jsonObject["id"]?.jsonPrimitive?.content
            if (id != null) {
                models.add(id)
            }
        }
        return models.sorted()
    }

    private suspend fun fetchMistralModels(provider: AiProvider, apiKey: String): List<String> {
        val url = "${provider.baseUrl}/v1/models"
        val response = httpClient.get(url) {
            headers.append("Authorization", "Bearer $apiKey")
        }.bodyAsText()

        val json = json.parseToJsonElement(response).jsonObject
        val models = mutableListOf<String>()
        json["data"]?.jsonArray?.forEach { model ->
            val id = model.jsonObject["id"]?.jsonPrimitive?.content
            if (id != null) {
                models.add(id)
            }
        }
        return models.sorted()
    }

    private suspend fun fetchOpenRouterModels(provider: AiProvider, apiKey: String): List<String> {
        val url = "${provider.baseUrl}/api/v1/models"
        val response = httpClient.get(url) {
            headers.append("Authorization", "Bearer $apiKey")
        }.bodyAsText()

        val json = json.parseToJsonElement(response).jsonObject
        val models = mutableListOf<String>()
        json["data"]?.jsonArray?.forEach { model ->
            val id = model.jsonObject["id"]?.jsonPrimitive?.content
            if (id != null) {
                models.add(id)
            }
        }
        return models.sorted()
    }

    private suspend fun fetchGeminiModels(provider: AiProvider, apiKey: String): List<String> {
        return listOf("gemini-pro", "gemini-pro-vision")
    }
}
