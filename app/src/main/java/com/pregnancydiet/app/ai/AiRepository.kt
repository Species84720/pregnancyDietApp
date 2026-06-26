package com.pregnancydiet.app.ai

interface AiRepository {
    suspend fun generateSummary(request: AiSummaryRequest): AiSummaryResult
}

class DefaultAiRepository(
    private val provider: AiProvider = AiDependencyProvider.aiProvider(),
    private val parser: AiResponseParser = AiResponseParser(),
) : AiRepository {
    override suspend fun generateSummary(request: AiSummaryRequest): AiSummaryResult {
        val result = when (request.requestType) {
            AiRequestType.DailyNutritionSummary -> provider.generateDietPlan(request.toDietAiRequest())
            AiRequestType.SymptomExplanation -> provider.analyzeSymptoms(request.toSymptomAiRequest())
            AiRequestType.WeeklySummary -> provider.generatePregnancyAdvice(request.toPregnancyAiRequest())
        }
        return when (result) {
            is AiResult.Success -> parser.parse(result.data.toRawText(), request)
            is AiResult.RateLimited -> AiPromptGuardrails.fallbackFor(request, result.message)
            is AiResult.QuotaExceeded -> AiPromptGuardrails.fallbackFor(request, result.message)
            is AiResult.Unauthorized -> AiPromptGuardrails.fallbackFor(request, result.message)
            is AiResult.NetworkError -> AiPromptGuardrails.fallbackFor(request, result.message)
            is AiResult.InvalidResponse -> AiPromptGuardrails.fallbackFor(request, result.message)
            is AiResult.SafetyBlocked -> AiPromptGuardrails.fallbackFor(request, result.message)
            is AiResult.SetupRequired -> AiPromptGuardrails.fallbackFor(request, result.message)
        }
    }
}

private fun AiSummaryRequest.toDietAiRequest(): DietAiRequest = DietAiRequest(
    pregnancyWeek = pregnancyWeek,
    trimester = trimester,
    allergies = allergies,
    dietaryRestrictions = dietaryRestrictions,
    medicalConditions = medicalConditions,
    nutritionGaps = detectedGaps,
    foodsToday = foodsToday,
)

private fun AiSummaryRequest.toSymptomAiRequest(): SymptomAiRequest = SymptomAiRequest(
    pregnancyWeek = pregnancyWeek,
    trimester = trimester,
    symptoms = symptomsToday,
    redFlagDetected = redFlagDetectedByApp,
    redFlagReasons = redFlagReasons,
)

private fun AiSummaryRequest.toPregnancyAiRequest(): PregnancyAiRequest = PregnancyAiRequest(
    pregnancyWeek = pregnancyWeek,
    trimester = trimester,
    symptoms = symptomsToday,
    nutritionGaps = detectedGaps.ifEmpty { weeklyRepeatedGaps },
    redFlagDetected = redFlagDetectedByApp,
    redFlagReasons = redFlagReasons,
)

private fun Any.toRawText(): String = when (this) {
    is PregnancyAiResponse -> text
    is DietAiResponse -> text
    is SymptomAiResponse -> text
    is MedicationSupplementAiResponse -> text
    else -> toString()
}
