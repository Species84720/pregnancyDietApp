package com.pregnancydiet.app.ai

data class PregnancyAiRequest(
    val pregnancyWeek: Int?,
    val trimester: Int?,
    val symptoms: List<AiSymptomContext> = emptyList(),
    val nutritionGaps: List<String> = emptyList(),
    val nutritionTotals: AiNutrientPayload = AiNutrientPayload(),
    val nutritionAlreadyProcessedByAi: Boolean = false,
    val redFlagDetected: Boolean = false,
    val redFlagReasons: List<String> = emptyList(),
)

data class DietAiRequest(
    val pregnancyWeek: Int?,
    val trimester: Int?,
    val allergies: List<String> = emptyList(),
    val dietaryRestrictions: List<String> = emptyList(),
    val medicalConditions: List<String> = emptyList(),
    val nutritionGaps: List<String> = emptyList(),
    val foodsToday: List<AiFoodContext> = emptyList(),
    val nutritionTotals: AiNutrientPayload = AiNutrientPayload(),
    val nutritionAlreadyProcessedByAi: Boolean = false,
)

data class SymptomAiRequest(
    val pregnancyWeek: Int?,
    val trimester: Int?,
    val symptoms: List<AiSymptomContext>,
    val redFlagDetected: Boolean,
    val redFlagReasons: List<String> = emptyList(),
)

data class MedicationSupplementAiRequest(
    val pregnancyWeek: Int?,
    val trimester: Int?,
    val supplements: List<AiSupplementContext>,
    val question: String = "Review logged pregnancy supplements for educational safety reminders.",
)

data class PregnancyAiResponse(val text: String)
data class DietAiResponse(val text: String)
data class SymptomAiResponse(val text: String)
data class MedicationSupplementAiResponse(val text: String)

sealed class AiResult<out T> {
    data class Success<T>(val data: T) : AiResult<T>()
    data class RateLimited(val retryAfterMillis: Long?, val message: String) : AiResult<Nothing>()
    data class QuotaExceeded(val message: String) : AiResult<Nothing>()
    data class Unauthorized(val message: String) : AiResult<Nothing>()
    data class NetworkError(val message: String, val throwable: Throwable? = null) : AiResult<Nothing>()
    data class InvalidResponse(val message: String) : AiResult<Nothing>()
    data class SafetyBlocked(val message: String) : AiResult<Nothing>()
    data class SetupRequired(val message: String) : AiResult<Nothing>()
}

interface AiProvider {
    suspend fun generatePregnancyAdvice(request: PregnancyAiRequest): AiResult<PregnancyAiResponse>
    suspend fun generateDietPlan(request: DietAiRequest): AiResult<DietAiResponse>
    suspend fun analyzeSymptoms(request: SymptomAiRequest): AiResult<SymptomAiResponse>
    suspend fun reviewMedicationOrSupplement(request: MedicationSupplementAiRequest): AiResult<MedicationSupplementAiResponse>
}

class FakeAiProvider : AiProvider {
    override suspend fun generatePregnancyAdvice(request: PregnancyAiRequest): AiResult<PregnancyAiResponse> = AiResult.Success(PregnancyAiResponse("Educational pregnancy guidance fallback."))
    override suspend fun generateDietPlan(request: DietAiRequest): AiResult<DietAiResponse> = AiResult.Success(DietAiResponse("Educational diet plan fallback."))
    override suspend fun analyzeSymptoms(request: SymptomAiRequest): AiResult<SymptomAiResponse> = AiResult.Success(SymptomAiResponse("Educational symptom context fallback."))
    override suspend fun reviewMedicationOrSupplement(request: MedicationSupplementAiRequest): AiResult<MedicationSupplementAiResponse> = AiResult.Success(MedicationSupplementAiResponse("Check supplement changes with your gynecologist."))
}

class BackendAiProvider(
    private val aiService: AiService = SecureBackendAiServiceContract(),
) : AiProvider {
    override suspend fun generatePregnancyAdvice(request: PregnancyAiRequest): AiResult<PregnancyAiResponse> = AiResult.SetupRequired("Backend AI provider is not configured for frontend access mode.")
    override suspend fun generateDietPlan(request: DietAiRequest): AiResult<DietAiResponse> = AiResult.SetupRequired("Backend AI provider is not configured for frontend access mode.")
    override suspend fun analyzeSymptoms(request: SymptomAiRequest): AiResult<SymptomAiResponse> = AiResult.SetupRequired("Backend AI provider is not configured for frontend access mode.")
    override suspend fun reviewMedicationOrSupplement(request: MedicationSupplementAiRequest): AiResult<MedicationSupplementAiResponse> = AiResult.SetupRequired("Backend AI provider is not configured for frontend access mode.")
}