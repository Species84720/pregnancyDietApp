package com.pregnancydiet.app.ai

import java.time.LocalDateTime

data class AiSummaryRecord(
    val id: String = "",
    val requestType: AiRequestType,
    val date: String? = null,
    val weekId: String? = null,
    val pregnancyProfileId: String? = null,
    val inputContextVersion: String = AiPromptGuardrails.INPUT_CONTEXT_VERSION,
    val summary: String,
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
    val disclaimer: String = AiPromptGuardrails.DISCLAIMER,
    val fallback: Boolean = false,
    val fallbackReason: String? = null,
    val createdAtIso: String = LocalDateTime.now().toString(),
)

object AiSummaryRecordFactory {
    fun fromResult(
        request: AiSummaryRequest,
        result: AiSummaryResult,
        pregnancyProfileId: String?,
    ): AiSummaryRecord = when (result) {
        is AiSummaryResult.Success -> result.response.toRecord(
            request = request,
            pregnancyProfileId = pregnancyProfileId,
        )
        is AiSummaryResult.Fallback -> result.fallback.toRecord(
            request = request,
            pregnancyProfileId = pregnancyProfileId,
            reason = result.reason,
        )
    }

    private fun AiSummaryResponse.toRecord(
        request: AiSummaryRequest,
        pregnancyProfileId: String?,
    ): AiSummaryRecord = AiSummaryRecord(
        requestType = request.requestType,
        date = request.date,
        weekId = request.weekId,
        pregnancyProfileId = pregnancyProfileId,
        inputContextVersion = request.inputContextVersion,
        summary = summary,
        stageContext = stageContext,
        nutritionEstimates = nutritionEstimates,
        nutritionEstimateSource = nutritionEstimateSource,
        nutritionEstimateNote = nutritionEstimateNote,
        nutritionGaps = nutritionGaps,
        recommendations = recommendations,
        safetyWarnings = safetyWarnings,
        symptomGuidance = symptomGuidance,
        weightContext = weightContext,
        urgentWarning = urgentWarning,
        urgentReasons = urgentReasons,
        nextSteps = nextSteps,
        disclaimer = disclaimer,
        fallback = false,
    )

    private fun AiFallbackSummary.toRecord(
        request: AiSummaryRequest,
        pregnancyProfileId: String?,
        reason: String,
    ): AiSummaryRecord {
        val localEstimateMerge = AiNutritionEstimateMerger.merge(
            aiEstimates = emptyMap(),
            localTotals = request.nutritionTotals,
        )
        return AiSummaryRecord(
            requestType = request.requestType,
            date = request.date,
            weekId = request.weekId,
            pregnancyProfileId = pregnancyProfileId,
            inputContextVersion = request.inputContextVersion,
            summary = message,
            stageContext = if (localNutritionGaps.isEmpty()) {
                "Use your saved logs and deterministic nutrition summaries for tracking while AI is unavailable."
            } else {
                "Local nutrition gaps detected: ${localNutritionGaps.joinToString()}"
            },
            nutritionEstimates = localEstimateMerge.estimates,
            nutritionEstimateSource = localEstimateMerge.source,
            nutritionEstimateNote = localEstimateMerge.note,
            urgentWarning = urgentWarning,
            urgentReasons = urgentReasons,
            nextSteps = if (urgentWarning) {
                listOf("Because a red-flag symptom was reported, seek medical advice urgently.")
            } else {
                listOf("Review your local nutrition gaps and contact your care team for medical concerns.")
            },
            disclaimer = disclaimer,
            fallback = true,
            fallbackReason = reason,
        )
    }
}
