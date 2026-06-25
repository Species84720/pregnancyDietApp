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
    val proteinGrams: Double = 0.0,
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
    val nutritionGaps: List<AiNutritionGapGuidance> = emptyList(),
    val symptomGuidance: AiSymptomGuidance? = null,
    val weightContext: AiWeightContext? = null,
    val urgentWarning: Boolean = false,
    val urgentReasons: List<String> = emptyList(),
    val nextSteps: List<String> = emptyList(),
    val disclaimer: String = "",
)

@Serializable
data class AiNutritionGapGuidance(
    val nutrient: String,
    val status: String,
    val explanation: String,
    val foodSuggestions: List<String> = emptyList(),
    val safetyNote: String = "",
)

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
