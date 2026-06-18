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

class OpenAiAdapterTest {

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
    fun `streams text chunks from SSE response`() = runTest {
        val sseBody = buildString {
            appendLine("""data: {"choices":[{"delta":{"content":"Hello"}}]}""")
            appendLine("""data: {"choices":[{"delta":{"content":" world"}}]}""")
            appendLine("data: [DONE]")
        }
        val client = buildClient(sseBody)
        val adapter = OpenAiAdapter(client)

        val chunks = adapter.stream(
            messages = listOf(AiMessage("user", "hi")),
            systemPrompt = "You are helpful.",
            apiKey = "test-key",
            model = "gpt-4o",
            baseUrl = "https://api.openai.com"
        ).toList()

        assertEquals(listOf("Hello", " world"), chunks)
    }

    @Test
    fun `skips empty delta lines`() = runTest {
        val sseBody = buildString {
            appendLine("""data: {"choices":[{"delta":{}}]}""")
            appendLine("""data: {"choices":[{"delta":{"content":"Hi"}}]}""")
            appendLine("data: [DONE]")
        }
        val client = buildClient(sseBody)
        val adapter = OpenAiAdapter(client)

        val chunks = adapter.stream(
            listOf(AiMessage("user", "hi")), "", "key", "gpt-4o", "https://api.openai.com"
        ).toList()

        assertEquals(listOf("Hi"), chunks)
    }
}
