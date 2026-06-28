package com.pregnancydiet.app.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AiRequestType(val wireValue: String) {
    @SerialName("daily_nutrition_summary")
    DailyNutritionSummary("daily_nutrition_summary"),

    @SerialName("symptom_explanation")
    SymptomExplanation("symptom_explanation"),

    @SerialName("weekly_summary")
    WeeklySummary("weekly_summary"),
}

@Serializable
data class AiSummaryRequest(
    val requestType: AiRequestType,
    val date: String? = null,
    val weekId: String? = null,
    val pregnancyWeek: Int? = null,
    val trimester: Int? = null,
    val estimatedDueDate: String? = null,
    val pregnancyType: String? = null,
    val heightCm: Double? = null,
    val prePregnancyWeightKg: Double? = null,
    val currentWeightKg: Double? = null,
    val dietaryRestrictions: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val medicalConditions: List<String> = emptyList(),
    val doctorNotes: String = "",
    val symptomsToday: List<AiSymptomContext> = emptyList(),
    val foodsToday: List<AiFoodContext> = emptyList(),
    val supplementsToday: List<AiSupplementContext> = emptyList(),
    val nutritionTotals: AiNutrientPayload = AiNutrientPayload(),
    val nutritionTargets: AiNutrientPayload = AiNutrientPayload(),
    val detectedGaps: List<String> = emptyList(),
    val weeklyRepeatedGaps: List<String> = emptyList(),
    val redFlagDetectedByApp: Boolean = false,
    val redFlagReasons: List<String> = emptyList(),
    val systemPromptVersion: String = AiPromptGuardrails.SYSTEM_PROMPT_VERSION,
    val inputContextVersion: String = AiPromptGuardrails.INPUT_CONTEXT_VERSION,
    val guardrails: List<String> = AiPromptGuardrails.guardrailRules,
)

@Serializable
data class AiSymptomContext(
    val name: String,
    val severity: Int,
    val duration: String,
    val notes: String,
)

@Serializable
data class AiFoodContext(
    val foodName: String,
    val quantity: Double,
    val unit: String,
    val weightGrams: Double?,
    val nutrition: AiNutrientPayload,
)

@Serializable
data class AiSupplementContext(
    val name: String,
    val dose: String,
    val taken: Boolean,
)

@Serializable
data class AiNutrientPayload(
    val calories: Double = 0.0,
    val caloriesKcal: Double = calories,
    val proteinGrams: Double = 0.0,
    val carbsGrams: Double = 0.0,
    val fatGrams: Double = 0.0,
    val fiberGrams: Double = 0.0,
    val folateMcg: Double = 0.0,
    val ironMg: Double = 0.0,
    val calciumMg: Double = 0.0,
    val vitaminDMcg: Double = 0.0,
    val vitaminB12Mcg: Double = 0.0,
    val iodineMcg: Double = 0.0,
    val omega3Mg: Double = 0.0,
    val cholineMg: Double = 0.0,
    val waterMl: Double = 0.0,
)

@Serializable
data class AiSummaryResponse(
    val summary: String = "",
    val stageContext: String = "",
    val nutritionEstimates: AiNutritionEstimates = AiNutritionEstimates(),
    val nutritionEstimateSource: AiNutritionEstimateSource = AiNutritionEstimateSource.LocalFallback,
    val nutritionEstimateNote: String = "",
    val nutritionGaps: List<AiNutritionGapGuidance> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val safetyWarnings: List<String> = emptyList(),
    val symptomGuidance: AiSymptomGuidance? = null,
    val weightContext: AiWeightContext? = null,
    val urgentWarning: Boolean = false,
    val urgentReasons: List<String> = emptyList(),
    val nextSteps: List<String> = emptyList(),
    val disclaimer: String = "",
)

@Serializable
data class AiNutritionGapGuidance(
    val nutrientKey: String = "unknown",
    val displayName: String = nutrientKey,
    val status: String = "unknown",
    val explanation: String = GENERIC_NUTRITION_GAP_EXPLANATION,
    val foodSuggestions: List<String> = emptyList(),
    val safetyNote: String = DEFAULT_NUTRITION_GAP_SAFETY_NOTE,
) {
    val nutrient: String
        get() = displayName.ifBlank { nutrientKey.ifBlank { "Nutrient" } }
}

@Serializable
data class AiNutritionEstimate(
    val value: Double = 0.0,
    val confidence: String = "low",
    val explanation: String = DEFAULT_AI_ESTIMATE_EXPLANATION,
    val source: String = "ai",
)

@Serializable
data class AiNutritionEstimates(
    val caloriesKcal: AiNutritionEstimate = AiNutritionEstimate(),
    val proteinGrams: AiNutritionEstimate = AiNutritionEstimate(),
    val carbsGrams: AiNutritionEstimate = AiNutritionEstimate(),
    val fatGrams: AiNutritionEstimate = AiNutritionEstimate(),
    val fiberGrams: AiNutritionEstimate = AiNutritionEstimate(),
    val folateMcg: AiNutritionEstimate = AiNutritionEstimate(),
    val ironMg: AiNutritionEstimate = AiNutritionEstimate(),
    val calciumMg: AiNutritionEstimate = AiNutritionEstimate(),
    val vitaminDMcg: AiNutritionEstimate = AiNutritionEstimate(),
    val vitaminB12Mcg: AiNutritionEstimate = AiNutritionEstimate(),
    val iodineMcg: AiNutritionEstimate = AiNutritionEstimate(),
    val omega3Mg: AiNutritionEstimate = AiNutritionEstimate(),
    val cholineMg: AiNutritionEstimate = AiNutritionEstimate(),
    val waterMl: AiNutritionEstimate = AiNutritionEstimate(),
)

@Serializable
enum class AiNutritionEstimateSource(val label: String, val wireValue: String) {
    @SerialName("ai_assisted")
    AiAssisted("AI-assisted estimate", "ai_assisted"),

    @SerialName("local_fallback")
    LocalFallback("Local fallback estimate", "local_fallback"),

    @SerialName("mixed_ai_local")
    MixedAiLocal("Mixed AI/local estimate", "mixed_ai_local"),
}

@Serializable
data class AiSymptomGuidance(
    val severity: String,
    val commonContext: String,
    val selfCare: List<String> = emptyList(),
    val contactDoctorIf: List<String> = emptyList(),
)

@Serializable
data class AiWeightContext(
    val summary: String,
    val doctorDiscussionRecommended: Boolean = false,
)

data class AiFallbackSummary(
    val message: String,
    val localNutritionGaps: List<String>,
    val urgentWarning: Boolean,
    val urgentReasons: List<String>,
    val disclaimer: String,
)

sealed class AiSummaryResult {
    data class Success(val response: AiSummaryResponse) : AiSummaryResult()
    data class Fallback(val fallback: AiFallbackSummary, val reason: String) : AiSummaryResult()
}

const val GENERIC_NUTRITION_GAP_EXPLANATION = "This nutrient may need attention based on today's logged foods and pregnancy nutrition needs."
const val DEFAULT_NUTRITION_GAP_SAFETY_NOTE = "For medical concerns or supplement changes, contact your gynecologist."
const val DEFAULT_AI_ESTIMATE_EXPLANATION = "Estimated by AI from logged food data."
const val DEFAULT_LOCAL_ESTIMATE_EXPLANATION = "Estimated locally from logged food data."
