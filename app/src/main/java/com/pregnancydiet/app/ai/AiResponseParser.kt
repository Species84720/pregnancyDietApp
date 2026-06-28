package com.pregnancydiet.app.ai

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
            val root = json.parseToJsonElement(jsonObject).jsonObject
            val response = root.toAiSummaryResponse(request)
            AiSafetyValidator.validate(response, request)
        } catch (exception: IllegalArgumentException) {
            AiPromptGuardrails.fallbackFor(request, "AI response could not be parsed: ${exception.message.orEmpty()}")
        } catch (exception: SerializationException) {
            AiPromptGuardrails.fallbackFor(request, "AI response did not match the expected schema: ${exception.message.orEmpty()}")
        }
    }

    private fun String.extractJsonObject(): String? {
        val trimmed = trim().removeMarkdownFenceMarkers()
        trimmed.forEachIndexed { index, character ->
            if (character == '{') {
                findBalancedJsonEnd(startIndex = index, input = trimmed)?.let { endIndex ->
                    return trimmed.substring(index, endIndex + 1)
                }
            }
        }
        return null
    }

    private fun JsonObject.toAiSummaryResponse(request: AiSummaryRequest): AiSummaryResponse {
        val summary = string("summary").takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("AI response did not include a summary.")
        val mergeResult = AiNutritionEstimateMerger.merge(
            aiEstimates = this["nutritionEstimates"].toAiEstimateMap(),
            localTotals = request.nutritionTotals,
        )
        val recommendations = stringList("recommendations")
        val nextSteps = stringList("nextSteps").ifEmpty { recommendations }
        val safetyWarnings = stringList("safetyWarnings")
        return AiSummaryResponse(
            summary = summary,
            stageContext = string("stageContext"),
            nutritionEstimates = mergeResult.estimates,
            nutritionEstimateSource = mergeResult.source,
            nutritionEstimateNote = mergeResult.note,
            nutritionGaps = (this["nutritionGaps"] as? JsonArray).toNutritionGaps(),
            recommendations = recommendations,
            safetyWarnings = safetyWarnings,
            symptomGuidance = decodeNullable("symptomGuidance"),
            weightContext = decodeNullable("weightContext"),
            urgentWarning = boolean("urgentWarning"),
            urgentReasons = stringList("urgentReasons"),
            nextSteps = nextSteps,
            disclaimer = string("disclaimer"),
        )
    }

    private inline fun <reified T> JsonObject.decodeNullable(key: String): T? = runCatching {
        val element = this[key]?.takeUnless { it is JsonNull } ?: return@runCatching null
        json.decodeFromJsonElement<T>(element)
    }.getOrNull()

    private fun JsonElement?.toAiEstimateMap(): Map<String, AiNutritionEstimate> {
        if (this == null || this is JsonNull) return emptyMap()
        return when (this) {
            is JsonObject -> AiNutritionEstimateMerger.fieldKeys.mapNotNull { key ->
                val element = this[key]?.takeUnless { it is JsonNull } ?: return@mapNotNull null
                key to element.toAiEstimate()
            }.toMap()
            is JsonArray -> mapNotNull { element ->
                val item = element as? JsonObject ?: return@mapNotNull null
                val key = item.string("key")
                    .ifBlank { item.string("nutrientKey") }
                    .ifBlank { item.string("name") }
                    .takeIf { it in AiNutritionEstimateMerger.fieldKeys }
                    ?: return@mapNotNull null
                key to item.toAiEstimate()
            }.toMap()
            else -> emptyMap()
        }
    }

    private fun JsonElement.toAiEstimate(): AiNutritionEstimate = when (this) {
        is JsonObject -> AiNutritionEstimate(
            value = number("value"),
            confidence = string("confidence").ifBlank { "low" },
            explanation = string("explanation").ifBlank { DEFAULT_AI_ESTIMATE_EXPLANATION },
            source = "ai",
        )
        is JsonPrimitive -> AiNutritionEstimate(
            value = doubleOrNull ?: 0.0,
            confidence = "low",
            explanation = DEFAULT_AI_ESTIMATE_EXPLANATION,
            source = "ai",
        )
        else -> AiNutritionEstimate(
            value = 0.0,
            confidence = "low",
            explanation = DEFAULT_AI_ESTIMATE_EXPLANATION,
            source = "ai",
        )
    }

    private fun JsonArray?.toNutritionGaps(): List<AiNutritionGapGuidance> {
        if (this == null) return emptyList()
        return mapNotNull { element ->
            when (element) {
                is JsonObject -> element.toNutritionGap()
                is JsonPrimitive -> element.contentOrNull?.takeIf { it.isNotBlank() }?.let { text ->
                    AiNutritionGapGuidance(
                        nutrientKey = text.toNutrientKey(),
                        displayName = text,
                    )
                }
                else -> null
            }
        }
    }

    private fun JsonObject.toNutritionGap(): AiNutritionGapGuidance {
        val nutrientKey = string("nutrientKey")
            .ifBlank { string("nutrient") }
            .ifBlank { string("key") }
            .ifBlank { "unknown" }
        val displayName = string("displayName")
            .ifBlank { string("nutrient") }
            .ifBlank { nutrientKey.toDisplayName() }
        return AiNutritionGapGuidance(
            nutrientKey = nutrientKey,
            displayName = displayName,
            status = string("status").ifBlank { "unknown" },
            explanation = string("explanation").ifBlank { GENERIC_NUTRITION_GAP_EXPLANATION },
            foodSuggestions = stringList("foodSuggestions"),
            safetyNote = string("safetyNote").ifBlank { DEFAULT_NUTRITION_GAP_SAFETY_NOTE },
        )
    }

    private fun JsonObject.string(key: String): String = (this[key] as? JsonPrimitive)?.contentOrNull.orEmpty()

    private fun JsonObject.number(key: String): Double = (this[key] as? JsonPrimitive)?.doubleOrNull ?: 0.0

    private fun JsonObject.boolean(key: String): Boolean = (this[key] as? JsonPrimitive)?.booleanOrNull ?: false

    private fun JsonObject.stringList(key: String): List<String> = when (val element = this[key]) {
        is JsonArray -> element.mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.takeIf(String::isNotBlank) }
        is JsonPrimitive -> element.contentOrNull?.takeIf(String::isNotBlank)?.let(::listOf).orEmpty()
        else -> emptyList()
    }

    private fun String.removeMarkdownFenceMarkers(): String = lines()
        .filterNot { it.trim().startsWith("```") }
        .joinToString("\n")
        .trim()

    private fun findBalancedJsonEnd(startIndex: Int, input: String): Int? {
        var depth = 0
        var inString = false
        var escaped = false
        for (index in startIndex until input.length) {
            val character = input[index]
            if (inString) {
                if (escaped) {
                    escaped = false
                } else if (character == '\\') {
                    escaped = true
                } else if (character == '"') {
                    inString = false
                }
            } else {
                when (character) {
                    '"' -> inString = true
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) return index
                    }
                }
            }
        }
        return null
    }

    private fun String.toNutrientKey(): String = trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
        .ifBlank { "unknown" }

    private fun String.toDisplayName(): String = replace(Regex("(?<=[a-z])(?=[A-Z])"), " ")
        .replace('_', ' ')
        .replaceFirstChar { it.uppercase() }
}
