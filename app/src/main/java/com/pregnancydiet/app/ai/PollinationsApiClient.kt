package com.pregnancydiet.app.ai

import com.pregnancydiet.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class PollinationsApiClient(
    private val baseUrl: String = BuildConfig.POLLINATIONS_BASE_URL,
    private val genBaseUrl: String = BuildConfig.POLLINATIONS_GEN_BASE_URL,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true },
) {
    suspend fun generateText(
        prompt: String,
        credential: AiCredentialResolution,
        model: String? = null,
    ): AiResult<String> {
        val requestPlan = when (credential) {
            is AiCredentialResolution.FreeHourlyCredential -> credential.publicKey
            is AiCredentialResolution.UserAccountCredential -> credential.credential
            AiCredentialResolution.MissingFreeHourlyKey -> return AiResult.SetupRequired("Free hourly AI is not configured.")
            AiCredentialResolution.MissingUserCredential -> return AiResult.SetupRequired("Reconnect your Pollinations account or switch to free hourly AI.")
            AiCredentialResolution.InvalidUserCredential -> return AiResult.Unauthorized("Your Pollinations account connection needs to be updated.")
            AiCredentialResolution.UnsafeCredential -> return AiResult.SetupRequired("Unsafe Pollinations credential rejected.")
        }.toRequestPlan(credential, model)
        if (AiCredentialValidator.isUnsafeServerSecret(requestPlan.credentialValue)) {
            return AiResult.SetupRequired("Unsafe Pollinations credential rejected.")
        }

        val requestBuilder = Request.Builder().url(requestPlan.url(prompt))
        if (requestPlan.method == PollinationsRequestMethod.POST) {
            requestBuilder
                .header("Content-Type", JSON_MEDIA_TYPE.toString())
                .post(requestPlan.bodyJson(prompt).toRequestBody(JSON_MEDIA_TYPE))
        }
        if (requestPlan.sendAuthorizationHeader) {
            requestBuilder.header("Authorization", "Bearer ${requestPlan.credentialValue}")
        }
        val request = requestBuilder.build()

        return executeWithRetry(request, attempts = 2)
    }

    private suspend fun executeWithRetry(request: Request, attempts: Int): AiResult<String> {
        var lastError: IOException? = null
        repeat(attempts) { attempt ->
            val result = executeOnce(request)
            if (result !is AiResult.NetworkError || attempt == attempts - 1) return result
            lastError = result.throwable as? IOException
            kotlinx.coroutines.delay(250L * (attempt + 1))
        }
        return AiResult.NetworkError("Network error while contacting AI provider.", lastError)
    }

    private suspend fun executeOnce(request: Request): AiResult<String> = withContext(Dispatchers.IO) {
        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                when (response.code) {
                    200 -> parseSuccessfulBody(body)
                    400 -> if (body.contains("safety", ignoreCase = true)) {
                        AiResult.SafetyBlocked("AI request was blocked by provider safety rules.")
                    } else {
                        AiResult.InvalidResponse("AI provider could not process this request.")
                    }
                    401, 403 -> AiResult.Unauthorized("Your Pollinations access needs to be updated.")
                    402 -> AiResult.QuotaExceeded("Your Pollinations account does not have enough Pollen for this AI request.")
                    404 -> AiResult.InvalidResponse("The configured Pollinations model is not available.")
                    429 -> AiResult.RateLimited(
                        retryAfterMillis = response.header("Retry-After")?.toRetryAfterMillis()
                            ?: (System.currentTimeMillis() + TimeBuckets.ONE_HOUR_MILLIS),
                        message = "AI usage is currently limited.",
                    )
                    in 500..599 -> AiResult.NetworkError("AI provider is temporarily unavailable.", null)
                    else -> if (body.contains("quota", ignoreCase = true) || body.contains("credit", ignoreCase = true) || body.contains("budget", ignoreCase = true) || body.contains("payment", ignoreCase = true)) {
                        AiResult.QuotaExceeded("Your Pollinations allowance may be used up.")
                    } else {
                        AiResult.InvalidResponse("Unexpected AI provider response.")
                    }
                }
            }
        } catch (exception: IOException) {
            AiResult.NetworkError("Network error while contacting AI provider.", exception)
        } catch (exception: IllegalArgumentException) {
            AiResult.InvalidResponse("AI provider returned malformed data.")
        }
    }

    private fun parseSuccessfulBody(body: String): AiResult<String> {
        if (body.isBlank()) return AiResult.InvalidResponse("AI provider returned an empty response.")
        val parsedText = runCatching {
            val root = json.parseToJsonElement(body).jsonObject
            val choices = root["choices"] as? JsonArray
            choices?.firstOrNull()?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive
                ?.contentOrNull
                ?: root["text"]?.jsonPrimitive?.contentOrNull
                ?: root["response"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()
        return AiResult.Success(parsedText?.takeIf { it.isNotBlank() } ?: body)
    }

    private fun String.toRetryAfterMillis(): Long? {
        val seconds = trim().toLongOrNull() ?: return null
        return System.currentTimeMillis() + seconds.coerceAtLeast(0L) * 1000L
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()
        const val LEGACY_FREE_MODEL = "openai-fast"
        const val LOW_COST_ACCOUNT_MODEL = "nova-fast"
    }

    private data class PollinationsRequestPlan(
        val baseUrl: String,
        val model: String,
        val credentialValue: String,
        val sendAuthorizationHeader: Boolean,
        val includeReasoningEffort: Boolean,
        val method: PollinationsRequestMethod,
    )

    private enum class PollinationsRequestMethod { GET, POST }

    private fun String.toRequestPlan(
        credential: AiCredentialResolution,
        requestedModel: String?,
    ): PollinationsRequestPlan = when (credential) {
        is AiCredentialResolution.UserAccountCredential -> PollinationsRequestPlan(
            baseUrl = "${genBaseUrl.trimEnd('/')}/v1/chat/completions",
            model = requestedModel ?: LOW_COST_ACCOUNT_MODEL,
            credentialValue = this,
            sendAuthorizationHeader = true,
            includeReasoningEffort = false,
            method = PollinationsRequestMethod.POST,
        )
        else -> PollinationsRequestPlan(
            baseUrl = "${baseUrl.trimEnd('/')}/openai",
            model = requestedModel ?: LEGACY_FREE_MODEL,
            credentialValue = this,
            sendAuthorizationHeader = false,
            includeReasoningEffort = true,
            method = PollinationsRequestMethod.POST,
        )
    }

    private fun PollinationsRequestPlan.url(prompt: String): String = when (method) {
        PollinationsRequestMethod.POST -> baseUrl
        PollinationsRequestMethod.GET -> baseUrl
    }

    private fun PollinationsRequestPlan.bodyJson(prompt: String): String = buildJsonObject {
        put("model", JsonPrimitive(model))
        put("messages", buildJsonArray {
            add(buildJsonObject {
                put("role", JsonPrimitive("user"))
                put("content", JsonPrimitive(prompt))
            })
        })
        put("temperature", JsonPrimitive(0.2))
        put("max_tokens", JsonPrimitive(3500))
        if (includeReasoningEffort) put("reasoning_effort", JsonPrimitive("low"))
    }.toString()
}