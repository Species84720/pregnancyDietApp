package com.pregnancydiet.app.ai

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

class PollinationsApiClientTest {
    @Test
    fun `client handles 200`() = runBlocking {
        val client = clientFor(200, "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}")

        val result = client.generateText("prompt", AiCredentialResolution.FreeHourlyCredential("pk_public_safe"))

        assertTrue(result is AiResult.Success)
        assertEquals("ok", (result as AiResult.Success).data)
    }

    @Test
    fun `client handles unauthorized responses`() = runBlocking {
        val client = clientFor(401, "unauthorized")

        val result = client.generateText("prompt", AiCredentialResolution.FreeHourlyCredential("pk_public_safe"))

        assertTrue(result is AiResult.Unauthorized)
    }

    @Test
    fun `client handles 403 responses`() = runBlocking {
        val client = clientFor(403, "forbidden")

        val result = client.generateText("prompt", AiCredentialResolution.FreeHourlyCredential("pk_public_safe"))

        assertTrue(result is AiResult.Unauthorized)
    }

    @Test
    fun `client handles 429 with retry after`() = runBlocking {
        val retrySeconds = "120"
        val client = clientFor(429, "limited", "Retry-After" to retrySeconds)

        val result = client.generateText("prompt", AiCredentialResolution.FreeHourlyCredential("pk_public_safe"))

        assertTrue(result is AiResult.RateLimited)
        assertTrue((result as AiResult.RateLimited).retryAfterMillis != null)
    }

    @Test
    fun `client handles 429 without retry after`() = runBlocking {
        val client = clientFor(429, "limited")

        val result = client.generateText("prompt", AiCredentialResolution.FreeHourlyCredential("pk_public_safe"))

        assertTrue(result is AiResult.RateLimited)
        assertTrue((result as AiResult.RateLimited).retryAfterMillis != null)
    }

    @Test
    fun `client handles server error as network error`() = runBlocking {
        val client = clientFor(503, "down")

        val result = client.generateText("prompt", AiCredentialResolution.FreeHourlyCredential("pk_public_safe"))

        assertTrue(result is AiResult.NetworkError)
    }

    @Test
    fun `free hourly uses anonymous legacy openai fast post with low reasoning`() = runBlocking {
        val capturedRequest = AtomicReference<Request>()
        val client = clientForCapturing(
            code = 200,
            body = "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}",
            capturedRequest = capturedRequest,
        )

        val result = client.generateText("prompt", AiCredentialResolution.FreeHourlyCredential("pk_public_safe"))

        assertTrue(result is AiResult.Success)
        val request = capturedRequest.get()
        assertEquals("POST", request.method)
        assertEquals("https://example.test/openai", request.url.toString())
        assertNull(request.header("Authorization"))
        assertTrue(requestBody(request).contains("\"model\":\"openai-fast\""))
        assertTrue(requestBody(request).contains("\"reasoning_effort\":\"low\""))
    }

    @Test
    fun `user account uses gen api nova fast`() = runBlocking {
        val capturedRequest = AtomicReference<Request>()
        val client = clientForCapturing(
            code = 200,
            body = "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}",
            capturedRequest = capturedRequest,
        )

        val result = client.generateText("prompt", AiCredentialResolution.UserAccountCredential("pk_user_with_budget"))

        assertTrue(result is AiResult.Success)
        val request = capturedRequest.get()
        assertEquals("POST", request.method)
        assertEquals("https://gen.example.test/v1/chat/completions", request.url.toString())
        assertEquals("Bearer pk_user_with_budget", request.header("Authorization"))
        assertTrue(requestBody(request).contains("\"model\":\"nova-fast\""))
    }

    @Test
    fun `client maps payment required to quota exceeded`() = runBlocking {
        val client = clientFor(402, "{\"error\":{\"message\":\"API key budget too low\"}}")

        val result = client.generateText("prompt", AiCredentialResolution.UserAccountCredential("pk_user_without_budget"))

        assertTrue(result is AiResult.QuotaExceeded)
    }

    private fun clientFor(
        code: Int,
        body: String,
        vararg headers: Pair<String, String>,
    ): PollinationsApiClient = clientForCapturing(
        code = code,
        body = body,
        capturedRequest = AtomicReference(),
        headers = headers,
    )

    private fun clientForCapturing(
        code: Int,
        body: String,
        capturedRequest: AtomicReference<Request>,
        vararg headers: Pair<String, String>,
    ): PollinationsApiClient {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                capturedRequest.set(chain.request())
                val builder = Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message("test")
                    .body(body.toResponseBody("application/json".toMediaType()))
                headers.forEach { (name, value) -> builder.header(name, value) }
                builder.build()
            })
            .build()
        return PollinationsApiClient(
            baseUrl = "https://example.test",
            genBaseUrl = "https://gen.example.test",
            httpClient = okHttpClient,
        )
    }

    private fun requestBody(request: Request): String {
        val buffer = okio.Buffer()
        request.body?.writeTo(buffer)
        return buffer.readUtf8()
    }
}
