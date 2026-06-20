package com.miniforge.app.ai

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class AnthropicAdapter @Inject constructor(private val client: HttpClient) : AiApiAdapter {

    @Serializable
    private data class Request(
        val model: String,
        val system: String,
        val messages: List<AiMessage>,
        val max_tokens: Int = 16000,
        val stream: Boolean = true
    )

    override fun stream(
        messages: List<AiMessage>,
        systemPrompt: String,
        apiKey: String,
        model: String,
        baseUrl: String
    ): Flow<String> = flow {
        client.preparePost("$baseUrl/v1/messages") {
            contentType(ContentType.Application.Json)
            headers {
                append("x-api-key", apiKey)
                append("anthropic-version", "2023-06-01")
            }
            setBody(Request(model = model, system = systemPrompt, messages = messages))
        }.execute { response ->
            if (response.status.value !in 200..299) {
                val error = response.bodyAsText()
                throw Exception("API error ${response.status.value}: ${error.take(300)}")
            }
            val channel = response.bodyAsChannel()
            var lastEvent = ""
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                when {
                    line.startsWith("event:") -> lastEvent = line.removePrefix("event:").trim()
                    line.startsWith("data:") && lastEvent == "content_block_delta" -> {
                        val data = line.removePrefix("data:").trim()
                        runCatching {
                            val text = Json.parseToJsonElement(data).jsonObject["delta"]!!
                                .jsonObject["text"]?.jsonPrimitive?.content
                            if (!text.isNullOrEmpty()) emit(text)
                        }
                    }
                }
            }
        }
    }
}
