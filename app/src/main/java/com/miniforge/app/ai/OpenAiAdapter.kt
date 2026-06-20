package com.miniforge.app.ai

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class OpenAiAdapter @Inject constructor(private val client: HttpClient) : AiApiAdapter {

    @Serializable
    private data class Request(
        val model: String,
        val messages: List<AiMessage>,
        val stream: Boolean = true,
        val max_tokens: Int = 16000
    )

    override fun stream(
        messages: List<AiMessage>,
        systemPrompt: String,
        apiKey: String,
        model: String,
        baseUrl: String
    ): Flow<String> = flow {
        val allMessages = buildList {
            if (systemPrompt.isNotBlank()) add(AiMessage("system", systemPrompt))
            addAll(messages)
        }
        client.preparePost("$baseUrl/v1/chat/completions") {
            contentType(ContentType.Application.Json)
            headers { append("Authorization", "Bearer $apiKey") }
            setBody(Request(model = model, messages = allMessages))
        }.execute { response ->
            Log.d("OpenAiAdapter", "HTTP ${response.status.value} ${response.status.description}")
            if (response.status.value !in 200..299) {
                val error = response.bodyAsText()
                Log.d("OpenAiAdapter", "Error body: ${error.take(500)}")
                throw Exception("API error ${response.status.value}: ${error.take(300)}")
            }
            val body = response.bodyAsText()
            Log.d("OpenAiAdapter", "Response size: ${body.length}")
            Log.d("OpenAiAdapter", "First 300 chars: ${body.take(300)}")

            // Check if it's SSE format (line starts with "data:") or regular JSON (starts with "{")
            if (body.startsWith("{")) {
                // Non-streaming response — parse as single JSON
                Log.d("OpenAiAdapter", "Got non-streaming JSON response")
                runCatching {
                    val json = Json.parseToJsonElement(body).jsonObject
                    val content = json["choices"]?.jsonArray?.get(0)?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.content
                    if (!content.isNullOrEmpty()) {
                        Log.d("OpenAiAdapter", "Extracted content length: ${content.length}")
                        emit(content)
                    } else {
                        Log.d("OpenAiAdapter", "No content in response")
                    }
                }.onFailure { e ->
                    Log.e("OpenAiAdapter", "Failed to parse JSON: ${e.message}")
                    throw e
                }
            } else {
                // SSE format — parse line by line
                Log.d("OpenAiAdapter", "Got SSE response")
                var dataCount = 0
                body.lines().forEach { line ->
                    if (line.startsWith("data:")) {
                        dataCount++
                        val data = line.removePrefix("data:").trim()
                        if (data == "[DONE]") return@forEach
                        runCatching {
                            val json = Json.parseToJsonElement(data).jsonObject
                            val content = json["choices"]?.jsonArray?.get(0)?.jsonObject?.get("delta")?.jsonObject?.get("content")?.jsonPrimitive?.content
                            if (!content.isNullOrEmpty()) emit(content)
                        }
                    }
                }
                Log.d("OpenAiAdapter", "SSE stream ended. Total data lines: $dataCount")
            }
        }
    }
}
