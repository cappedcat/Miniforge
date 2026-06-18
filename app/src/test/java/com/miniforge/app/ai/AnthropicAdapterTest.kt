package com.miniforge.app.ai

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class AnthropicAdapterTest {

    private fun buildClient(responseBody: String): HttpClient {
        val engine = MockEngine { _ ->
            respond(
                content = responseBody,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())
            )
        }
        return HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    }

    @Test
    fun `streams text from content_block_delta events`() = runTest {
        val sseBody = buildString {
            appendLine("event: content_block_delta")
            appendLine("""data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"Hello"}}""")
            appendLine()
            appendLine("event: content_block_delta")
            appendLine("""data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":" Claude"}}""")
            appendLine()
            appendLine("event: message_stop")
            appendLine("""data: {"type":"message_stop"}""")
        }
        val client = buildClient(sseBody)
        val adapter = AnthropicAdapter(client)

        val chunks = adapter.stream(
            messages = listOf(AiMessage("user", "hi")),
            systemPrompt = "You are helpful.",
            apiKey = "test-key",
            model = "claude-3-5-sonnet-20241022",
            baseUrl = "https://api.anthropic.com"
        ).toList()

        assertEquals(listOf("Hello", " Claude"), chunks)
    }
}
