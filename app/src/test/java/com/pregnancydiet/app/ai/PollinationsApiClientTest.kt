package com.pregnancydiet.app.ai

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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

    private fun clientFor(
        code: Int,
        body: String,
        vararg headers: Pair<String, String>,
    ): PollinationsApiClient {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
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
        return PollinationsApiClient(baseUrl = "https://example.test", httpClient = okHttpClient)
    }
}
