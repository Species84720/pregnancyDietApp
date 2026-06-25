package com.pregnancydiet.app.ai

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class AiResponseParser(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    },
) {
    fun parse(
        rawResponse: String,
        request: AiSummaryRequest,
    ): AiSummaryResult {
        val jsonObject = rawResponse.extractJsonObject()
            ?: return AiPromptGuardrails.fallbackFor(request, "AI response was not valid JSON.")
        return try {
            val response = json.decodeFromString<AiSummaryResponse>(jsonObject)
            AiSafetyValidator.validate(response, request)
        } catch (exception: IllegalArgumentException) {
            AiPromptGuardrails.fallbackFor(request, "AI response could not be parsed: ${exception.message.orEmpty()}")
        } catch (exception: SerializationException) {
            AiPromptGuardrails.fallbackFor(request, "AI response did not match the expected schema: ${exception.message.orEmpty()}")
        }
    }

    private fun String.extractJsonObject(): String? {
        val trimmed = trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val startIndex = trimmed.indexOf('{')
        val endIndex = trimmed.lastIndexOf('}')
        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) return null
        return trimmed.substring(startIndex, endIndex + 1)
    }
}
