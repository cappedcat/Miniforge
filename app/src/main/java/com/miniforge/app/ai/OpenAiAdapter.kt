package com.miniforge.app.ai

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.readUTF8Line
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
        val stream: Boolean = true
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
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (!line.startsWith("data:")) continue
                val data = line.removePrefix("data:").trim()
                if (data == "[DONE]") break
                runCatching {
                    val json = Json.parseToJsonElement(data).jsonObject
                    val content = json["choices"]!!.jsonArray[0].jsonObject["delta"]!!
                        .jsonObject["content"]?.jsonPrimitive?.content
                    if (!content.isNullOrEmpty()) emit(content)
                }
            }
        }
    }
}
