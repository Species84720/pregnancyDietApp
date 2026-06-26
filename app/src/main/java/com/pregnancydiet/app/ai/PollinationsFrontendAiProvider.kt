package com.pregnancydiet.app.ai

class PollinationsFrontendAiProvider(
    private val credentialResolver: AiCredentialResolver,
    private val usageRepository: AiUsageRepository,
    private val apiClient: PollinationsApiClient = PollinationsApiClient(),
    private val pregnancyAdvicePromptBuilder: PregnancyAdvicePromptBuilder = PregnancyAdvicePromptBuilder(),
    private val dietPlanPromptBuilder: DietPlanPromptBuilder = DietPlanPromptBuilder(),
    private val symptomAnalysisPromptBuilder: SymptomAnalysisPromptBuilder = SymptomAnalysisPromptBuilder(),
    private val medicationSupplementPromptBuilder: MedicationSupplementPromptBuilder = MedicationSupplementPromptBuilder(),
) : AiProvider {
    override suspend fun generatePregnancyAdvice(request: PregnancyAiRequest): AiResult<PregnancyAiResponse> = generate(
        feature = AiFeature.DAILY_ADVICE,
        prompt = pregnancyAdvicePromptBuilder.build(request),
        mapper = ::PregnancyAiResponse,
    )

    override suspend fun generateDietPlan(request: DietAiRequest): AiResult<DietAiResponse> = generate(
        feature = AiFeature.DIET_PLAN,
        prompt = dietPlanPromptBuilder.build(request),
        mapper = ::DietAiResponse,
    )

    override suspend fun analyzeSymptoms(request: SymptomAiRequest): AiResult<SymptomAiResponse> = generate(
        feature = AiFeature.SYMPTOM_ANALYSIS,
        prompt = symptomAnalysisPromptBuilder.build(request),
        mapper = ::SymptomAiResponse,
    )

    override suspend fun reviewMedicationOrSupplement(request: MedicationSupplementAiRequest): AiResult<MedicationSupplementAiResponse> = generate(
        feature = AiFeature.MEDICATION_SUPPLEMENT_CHECK,
        prompt = medicationSupplementPromptBuilder.build(request),
        mapper = ::MedicationSupplementAiResponse,
    )

    private suspend fun <T> generate(
        feature: AiFeature,
        prompt: String,
        mapper: (String) -> T,
    ): AiResult<T> {
        val credential = credentialResolver.resolveCredential()
        val accessMode = when (credential) {
            is AiCredentialResolution.UserAccountCredential -> AiAccessMode.USER_ACCOUNT
            else -> AiAccessMode.FREE_HOURLY
        }
        if (credential is AiCredentialResolution.MissingFreeHourlyKey || credential is AiCredentialResolution.MissingUserCredential || credential is AiCredentialResolution.InvalidUserCredential || credential is AiCredentialResolution.UnsafeCredential) {
            val message = credential.toSetupMessage()
            usageRepository.recordSetupRequired(feature, message, accessMode)
            return AiResult.SetupRequired(message)
        }

        val availability = usageRepository.canUseAi(feature)
        if (availability is AiAvailability.CoolingDown && accessMode == AiAccessMode.FREE_HOURLY) {
            return AiResult.RateLimited(availability.nextAvailableAtMillis, "AI usage is currently limited. Your next estimated free request is available later.")
        }
        if (availability is AiAvailability.AccountInvalid) return AiResult.Unauthorized(availability.message)
        if (availability is AiAvailability.QuotaExceeded) return AiResult.QuotaExceeded(availability.message)
        if (availability is AiAvailability.SetupRequired) return AiResult.SetupRequired(availability.message)

        val result = apiClient.generateText(prompt = prompt, credential = credential)
        return when (result) {
            is AiResult.Success -> {
                usageRepository.recordSuccess(feature, accessMode)
                AiResult.Success(mapper(result.data))
            }
            is AiResult.RateLimited -> {
                usageRepository.recordRateLimited(feature, result.retryAfterMillis, accessMode)
                result
            }
            is AiResult.QuotaExceeded -> {
                usageRepository.recordQuotaExceeded(feature, accessMode)
                result
            }
            is AiResult.Unauthorized -> {
                usageRepository.recordUnauthorized(feature, accessMode)
                result
            }
            is AiResult.NetworkError -> {
                usageRepository.recordNetworkError(feature, result.message, accessMode)
                result
            }
            is AiResult.InvalidResponse -> {
                usageRepository.recordInvalidResponse(feature, result.message, accessMode)
                result
            }
            is AiResult.SetupRequired -> {
                usageRepository.recordSetupRequired(feature, result.message, accessMode)
                result
            }
            is AiResult.SafetyBlocked -> result
        }
    }

    private fun AiCredentialResolution.toSetupMessage(): String = when (this) {
        AiCredentialResolution.MissingFreeHourlyKey -> "Free hourly AI is not configured."
        AiCredentialResolution.MissingUserCredential -> "Reconnect your Pollinations account or switch to free hourly AI."
        AiCredentialResolution.InvalidUserCredential -> "Your Pollinations account connection needs to be updated."
        AiCredentialResolution.UnsafeCredential -> "Unsafe Pollinations credential rejected."
        else -> "AI setup is required."
    }
}