package com.pregnancydiet.app.firebase

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.pregnancydiet.app.ai.DEFAULT_AI_ESTIMATE_EXPLANATION
import com.pregnancydiet.app.ai.DEFAULT_NUTRITION_GAP_SAFETY_NOTE
import com.pregnancydiet.app.ai.GENERIC_NUTRITION_GAP_EXPLANATION
import com.pregnancydiet.app.ai.AiNutritionGapGuidance
import com.pregnancydiet.app.ai.AiNutritionEstimate
import com.pregnancydiet.app.ai.AiNutritionEstimateSource
import com.pregnancydiet.app.ai.AiNutritionEstimates
import com.pregnancydiet.app.ai.AiPromptGuardrails
import com.pregnancydiet.app.ai.AiRepository
import com.pregnancydiet.app.ai.AiRequestBuilder
import com.pregnancydiet.app.ai.AiRequestType
import com.pregnancydiet.app.ai.AiSummaryRecord
import com.pregnancydiet.app.ai.AiSummaryRecordFactory
import com.pregnancydiet.app.ai.AiSymptomGuidance
import com.pregnancydiet.app.ai.AiWeightContext
import com.pregnancydiet.app.ai.DefaultAiRepository
import com.pregnancydiet.app.data.AiSummaryRepository
import com.pregnancydiet.app.model.NutrientAmounts
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.SymptomEntry
import com.pregnancydiet.app.model.SymptomLog
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.pregnancy.PregnancyCalculator
import com.pregnancydiet.app.pregnancy.PregnancyDatingInput
import com.pregnancydiet.app.pregnancy.PregnancyProgress
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

class FirestoreAiSummaryRepository(
    private val aiRepository: AiRepository = DefaultAiRepository(),
    private val requestBuilder: AiRequestBuilder = AiRequestBuilder(),
    private val nutritionRepository: FirestoreNutritionSummaryRepository = FirestoreNutritionSummaryRepository(),
    private val mealRepository: FirestoreMealLogRepository = FirestoreMealLogRepository(),
    private val supplementRepository: FirestoreSupplementRepository = FirestoreSupplementRepository(),
    private val pregnancyCalculator: PregnancyCalculator = PregnancyCalculator(),
) : AiSummaryRepository {
    override suspend fun generateDailyInsight(
        uid: String,
        date: LocalDate,
    ): Result<AiSummaryRecord> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val dailySummaryRef = userRef.collection(DAILY_NUTRITION_SUMMARIES_COLLECTION)
            .document(date.toString())
        val cachedRecord = dailySummaryRef.get().await().cachedAiSummaryRecord(
            expectedType = AiRequestType.DailyNutritionSummary,
            expectedDate = date.toString(),
            expectedWeekId = null,
            fallbackId = dailySummaryId(date),
            requiresAiNutrition = true,
        )
        if (cachedRecord != null) {
            saveAiSummaryLog(userRef, cachedRecord)
            return@runCatching cachedRecord
        }

        requireAiProcessingAllowed(uid)
        val context = loadActiveProfileContext(uid)
        val dailySummary = nutritionRepository.loadDailySummary(uid, date).getOrThrow()
            ?: nutritionRepository.generateAndSaveDailySummary(uid, date).getOrThrow()
        val meals = mealRepository.loadMealLogsForDate(uid, date).getOrThrow()
        val symptoms = loadSymptomLogsForDate(uid, date)
        val supplements = supplementRepository.loadSupplementsForDate(uid, date).getOrThrow()
        val request = requestBuilder.dailyNutritionSummary(
            date = date.toString(),
            pregnancyProfile = context.profile,
            nutritionSummary = dailySummary,
            mealsToday = meals,
            symptomsToday = symptoms,
            supplementsToday = supplements,
        )
        val result = aiRepository.generateSummary(request)
        val record = AiSummaryRecordFactory.fromResult(
            request = request,
            result = result,
            pregnancyProfileId = context.profile.id,
        ).copy(id = dailySummaryId(date))
        dailySummaryRef
            .set(
                mapOf(
                    "aiSummaryId" to record.id,
                    "aiSummary" to record.toFirestoreMap(),
                    "aiSummaryStatus" to record.summaryProcessingStatus,
                    "aiSummaryProcessedBy" to record.summaryProcessedBy,
                    "aiSummaryUpdatedAtIso" to record.createdAtIso,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ) + record.toNutritionProcessingFirestoreMap(),
                SetOptions.merge(),
            )
            .await()
        saveAiSummaryLog(userRef, record)
        record
    }

    override suspend fun generateSymptomGuidance(
        uid: String,
        date: LocalDate,
    ): Result<AiSummaryRecord> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val symptoms = loadSymptomLogsForDate(uid, date)
        val symptomLog = symptoms.firstOrNull()
            ?: error("Log a symptom for this date before generating symptom guidance.")
        val symptomSummaryRef = userRef.collection(SYMPTOM_LOGS_COLLECTION).document(symptomLog.id)
        val cachedRecord = symptomSummaryRef.get().await().cachedAiSummaryRecord(
            expectedType = AiRequestType.SymptomExplanation,
            expectedDate = date.toString(),
            expectedWeekId = null,
            fallbackId = symptomSummaryId(symptomLog.id),
            requiresAiNutrition = false,
        )
        if (cachedRecord != null) {
            saveAiSummaryLog(userRef, cachedRecord)
            return@runCatching cachedRecord
        }

        requireAiProcessingAllowed(uid)
        val context = loadActiveProfileContext(uid)
        val supplements = supplementRepository.loadSupplementsForDate(uid, date).getOrThrow()
        val request = requestBuilder.symptomExplanation(
            date = date.toString(),
            pregnancyProfile = context.profile,
            pregnancyWeek = symptomLog.pregnancyWeek ?: context.progress.pregnancyWeek,
            trimester = symptomLog.trimester ?: context.progress.trimester,
            symptomsToday = listOf(symptomLog),
            supplementsToday = supplements,
        )
        val result = aiRepository.generateSummary(request)
        val record = AiSummaryRecordFactory.fromResult(
            request = request,
            result = result,
            pregnancyProfileId = context.profile.id,
        ).copy(id = symptomSummaryId(symptomLog.id))
        symptomSummaryRef
            .set(
                mapOf(
                    "aiSummaryId" to record.id,
                    "aiSummary" to record.toFirestoreMap(),
                    "aiSummaryStatus" to record.summaryProcessingStatus,
                    "aiSummaryProcessedBy" to record.summaryProcessedBy,
                    "aiSummaryUpdatedAtIso" to record.createdAtIso,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
        saveAiSummaryLog(userRef, record)
        record
    }

    override suspend fun generateWeeklySummary(
        uid: String,
        endDate: LocalDate,
    ): Result<AiSummaryRecord> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val weekId = endDate.toWeekId()
        val weeklySummaryRef = userRef.collection(WEEKLY_SUMMARIES_COLLECTION).document(weekId)
        val cachedRecord = weeklySummaryRef.get().await().cachedAiSummaryRecord(
            expectedType = AiRequestType.WeeklySummary,
            expectedDate = null,
            expectedWeekId = weekId,
            fallbackId = weeklySummaryId(weekId),
            requiresAiNutrition = true,
        )
        if (cachedRecord != null) {
            saveAiSummaryLog(userRef, cachedRecord)
            return@runCatching cachedRecord
        }

        requireAiProcessingAllowed(uid)
        val context = loadActiveProfileContext(uid)
        val weeklyTrend = nutritionRepository.loadWeeklyTrend(uid, endDate).getOrThrow()
        val symptoms = loadSymptomLogsForRange(
            uid = uid,
            startDate = endDate.minusDays(6),
            endDate = endDate,
        )
        val request = requestBuilder.weeklySummary(
            weekId = weekId,
            endDate = endDate.toString(),
            pregnancyProfile = context.profile,
            pregnancyWeek = context.progress.pregnancyWeek,
            trimester = context.progress.trimester,
            weeklyTrend = weeklyTrend,
            symptomLogs = symptoms,
        )
        val result = aiRepository.generateSummary(request)
        val record = AiSummaryRecordFactory.fromResult(
            request = request,
            result = result,
            pregnancyProfileId = context.profile.id,
        ).copy(id = weeklySummaryId(weekId))
        weeklySummaryRef
            .set(
                mapOf(
                    "weekId" to weekId,
                    "pregnancyProfileId" to context.profile.id,
                    "pregnancyWeek" to context.progress.pregnancyWeek,
                    "trimester" to context.progress.trimester,
                    "daysIncluded" to weeklyTrend.daysIncluded,
                    "repeatedGaps" to weeklyTrend.repeatedGaps,
                    "aiSummaryId" to record.id,
                    "aiSummary" to record.toFirestoreMap(),
                    "aiSummaryStatus" to record.summaryProcessingStatus,
                    "aiSummaryProcessedBy" to record.summaryProcessedBy,
                    "aiSummaryUpdatedAtIso" to record.createdAtIso,
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "createdAt" to FieldValue.serverTimestamp(),
                ) + record.toNutritionProcessingFirestoreMap(),
                SetOptions.merge(),
            )
            .await()
        saveAiSummaryLog(userRef, record)
        record
    }

    private suspend fun loadActiveProfileContext(uid: String): AiProfileContext {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val userSnapshot = userRef.get().await()
        val activePregnancyProfileId = userSnapshot.getString("activePregnancyProfileId")
            ?.takeIf { it.isNotBlank() }
            ?: error("Complete onboarding before generating AI summaries.")
        val profileSnapshot = userRef.collection(PREGNANCY_PROFILES_COLLECTION)
            .document(activePregnancyProfileId)
            .get()
            .await()
        if (!profileSnapshot.exists()) error("Complete onboarding before generating AI summaries.")
        val profile = profileSnapshot.toPregnancyProfile()
        val progress = pregnancyCalculator.calculate(
            PregnancyDatingInput(
                dateFoundOut = profile.dateFoundOut.toLocalDateOrToday(),
                lastMenstrualPeriod = profile.lastMenstrualPeriod?.toLocalDateOrNull(),
                estimatedDueDate = profile.estimatedDueDate?.toLocalDateOrNull(),
                doctorConfirmedWeek = profile.doctorConfirmedWeek,
            ),
        )
        return AiProfileContext(profile = profile, progress = progress)
    }

    private suspend fun requireAiProcessingAllowed(uid: String) {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val allowed = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(PRIVACY_SETTINGS_COLLECTION)
            .document(DEFAULT_DOCUMENT_ID)
            .get()
            .await()
            .getBoolean("aiProcessingAllowed") ?: true
        require(allowed) {
            "AI summaries are turned off in Settings. Turn them on only if you want minimal structured logs sent through the backend for educational summaries."
        }
    }

    private suspend fun loadSymptomLogsForDate(uid: String, date: LocalDate): List<SymptomLog> {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SYMPTOM_LOGS_COLLECTION)
            .whereEqualTo("date", date.toString())
            .get()
            .await()
            .documents
            .map { it.toSymptomLog() }
            .sortedByDescending { it.id }
    }

    private suspend fun loadSymptomLogsForRange(
        uid: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<SymptomLog> {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SYMPTOM_LOGS_COLLECTION)
            .whereGreaterThanOrEqualTo("date", startDate.toString())
            .whereLessThanOrEqualTo("date", endDate.toString())
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toSymptomLog() }
    }

    private suspend fun saveAiSummaryLog(
        userRef: DocumentReference,
        record: AiSummaryRecord,
    ) {
        val recordWithId = record.copy(id = record.stableSummaryId())
        userRef.collection(AI_SUMMARIES_COLLECTION)
            .document(recordWithId.id)
            .set(recordWithId.toAiSummaryLogFirestoreMap(), SetOptions.merge())
            .await()
    }
}

private data class AiProfileContext(
    val profile: PregnancyProfile,
    val progress: PregnancyProgress,
)

private const val USERS_COLLECTION = "users"
private const val PREGNANCY_PROFILES_COLLECTION = "pregnancyProfiles"
private const val DAILY_NUTRITION_SUMMARIES_COLLECTION = "dailyNutritionSummaries"
private const val SYMPTOM_LOGS_COLLECTION = "symptomLogs"
private const val WEEKLY_SUMMARIES_COLLECTION = "weeklySummaries"
private const val AI_SUMMARIES_COLLECTION = "aiSummaries"
private const val PRIVACY_SETTINGS_COLLECTION = "privacySettings"
private const val DEFAULT_DOCUMENT_ID = "default"
private const val SUMMARY_STATUS_AI_PROCESSED = "ai_processed"
private const val SUMMARY_STATUS_FALLBACK = "fallback_saved"
private const val NUTRITION_STATUS_AI_PROCESSED = "ai_processed"
private const val NUTRITION_STATUS_LOCAL_FALLBACK = "local_fallback"
private const val PROCESSED_BY_AI = "ai"
private const val PROCESSED_BY_LOCAL_FALLBACK = "local_fallback"

private fun AiSummaryRecord.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "type" to requestType.wireValue,
    "date" to date,
    "weekId" to weekId,
    "pregnancyProfileId" to pregnancyProfileId,
    "inputContextVersion" to inputContextVersion,
    "summary" to summary,
    "stageContext" to stageContext,
    "nutritionEstimates" to nutritionEstimates.toFirestoreMap(),
    "nutritionEstimateSource" to nutritionEstimateSource.wireValue,
    "nutritionEstimateNote" to nutritionEstimateNote,
    "nutritionGaps" to nutritionGaps.map { it.toFirestoreMap() },
    "recommendations" to recommendations,
    "safetyWarnings" to safetyWarnings,
    "symptomGuidance" to symptomGuidance?.toFirestoreMap(),
    "weightContext" to weightContext?.toFirestoreMap(),
    "urgentWarning" to urgentWarning,
    "urgentReasons" to urgentReasons,
    "nextSteps" to nextSteps,
    "disclaimer" to disclaimer,
    "fallback" to fallback,
    "fallbackReason" to fallbackReason,
    "processingStatus" to summaryProcessingStatus,
    "processedBy" to summaryProcessedBy,
    "nutritionProcessed" to hasAiNutritionProcessed,
    "nutritionProcessedBy" to nutritionProcessedBy,
    "nutritionProcessingStatus" to nutritionProcessingStatus,
    "createdAtIso" to createdAtIso,
)

private fun AiSummaryRecord.toAiSummaryLogFirestoreMap(): Map<String, Any?> = toFirestoreMap() + mapOf(
    "analysisDate" to date,
    "analysisWeekId" to weekId,
    "loggedAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp(),
)

private fun AiSummaryRecord.toNutritionProcessingFirestoreMap(): Map<String, Any?> = mapOf(
    "aiNutritionEstimates" to nutritionEstimates.toFirestoreMap(),
    "aiNutritionTotals" to nutritionEstimates.toNutrientAmounts().toFirestoreMap(),
    "aiNutritionEstimateSource" to nutritionEstimateSource.wireValue,
    "aiNutritionEstimateNote" to nutritionEstimateNote,
    "aiNutritionProcessed" to hasAiNutritionProcessed,
    "nutritionProcessedBy" to nutritionProcessedBy,
    "nutritionProcessingStatus" to nutritionProcessingStatus,
    "nutritionProcessedAtIso" to createdAtIso,
    "nutritionProcessedAt" to FieldValue.serverTimestamp(),
)

private val AiSummaryRecord.hasAiNutritionProcessed: Boolean
    get() = !fallback && nutritionEstimateSource != AiNutritionEstimateSource.LocalFallback

private val AiSummaryRecord.summaryProcessingStatus: String
    get() = if (fallback) SUMMARY_STATUS_FALLBACK else SUMMARY_STATUS_AI_PROCESSED

private val AiSummaryRecord.summaryProcessedBy: String
    get() = if (fallback) PROCESSED_BY_LOCAL_FALLBACK else PROCESSED_BY_AI

private val AiSummaryRecord.nutritionProcessingStatus: String
    get() = if (hasAiNutritionProcessed) NUTRITION_STATUS_AI_PROCESSED else NUTRITION_STATUS_LOCAL_FALLBACK

private val AiSummaryRecord.nutritionProcessedBy: String
    get() = if (hasAiNutritionProcessed) PROCESSED_BY_AI else PROCESSED_BY_LOCAL_FALLBACK

private fun AiSummaryRecord.stableSummaryId(): String = id.ifBlank {
    when (requestType) {
        AiRequestType.DailyNutritionSummary -> dailySummaryId(date.orEmpty())
        AiRequestType.SymptomExplanation -> symptomSummaryId(date.orEmpty())
        AiRequestType.WeeklySummary -> weeklySummaryId(weekId.orEmpty())
    }
}.firestoreIdSafe()

private fun DocumentSnapshot.cachedAiSummaryRecord(
    expectedType: AiRequestType,
    expectedDate: String?,
    expectedWeekId: String?,
    fallbackId: String,
    requiresAiNutrition: Boolean,
): AiSummaryRecord? {
    val aiSummary = get("aiSummary") as? Map<*, *> ?: return null
    val record = aiSummary.toAiSummaryRecord(fallbackId) ?: return null
    if (record.requestType != expectedType) return null
    if (expectedDate != null && record.date != expectedDate) return null
    if (expectedWeekId != null && record.weekId != expectedWeekId) return null
    if (record.fallback) return null
    if (requiresAiNutrition && !record.hasAiNutritionProcessed) return null
    return record.copy(id = record.id.ifBlank { fallbackId }.firestoreIdSafe())
}

private fun Map<*, *>.toAiSummaryRecord(fallbackId: String): AiSummaryRecord? {
    val requestType = (this["type"] as? String).toAiRequestType() ?: return null
    val summary = this["summary"] as? String ?: return null
    return AiSummaryRecord(
        id = (this["id"] as? String).orEmpty().ifBlank { fallbackId }.firestoreIdSafe(),
        requestType = requestType,
        date = this["date"] as? String,
        weekId = this["weekId"] as? String,
        pregnancyProfileId = this["pregnancyProfileId"] as? String,
        inputContextVersion = this["inputContextVersion"] as? String ?: AiPromptGuardrails.INPUT_CONTEXT_VERSION,
        summary = summary,
        stageContext = this["stageContext"] as? String ?: "",
        nutritionEstimates = (this["nutritionEstimates"] as? Map<*, *>)?.toAiNutritionEstimates() ?: AiNutritionEstimates(),
        nutritionEstimateSource = (this["nutritionEstimateSource"] as? String).toAiNutritionEstimateSource(),
        nutritionEstimateNote = this["nutritionEstimateNote"] as? String ?: "",
        nutritionGaps = (this["nutritionGaps"] as? List<*>)
            ?.mapNotNull { it as? Map<*, *> }
            ?.map { it.toAiNutritionGapGuidance() }
            .orEmpty(),
        recommendations = getStringList("recommendations"),
        safetyWarnings = getStringList("safetyWarnings"),
        symptomGuidance = (this["symptomGuidance"] as? Map<*, *>)?.toAiSymptomGuidance(),
        weightContext = (this["weightContext"] as? Map<*, *>)?.toAiWeightContext(),
        urgentWarning = this["urgentWarning"] as? Boolean ?: false,
        urgentReasons = getStringList("urgentReasons"),
        nextSteps = getStringList("nextSteps"),
        disclaimer = this["disclaimer"] as? String ?: AiPromptGuardrails.DISCLAIMER,
        fallback = this["fallback"] as? Boolean ?: false,
        fallbackReason = this["fallbackReason"] as? String,
        createdAtIso = this["createdAtIso"] as? String ?: "",
    )
}

private fun AiNutritionGapGuidance.toFirestoreMap(): Map<String, Any?> = mapOf(
    "nutrient" to nutrient,
    "nutrientKey" to nutrientKey,
    "displayName" to displayName,
    "status" to status,
    "explanation" to explanation,
    "foodSuggestions" to foodSuggestions,
    "safetyNote" to safetyNote,
)

private fun AiNutritionEstimates.toFirestoreMap(): Map<String, Any?> = mapOf(
    "caloriesKcal" to caloriesKcal.toFirestoreMap(),
    "proteinGrams" to proteinGrams.toFirestoreMap(),
    "carbsGrams" to carbsGrams.toFirestoreMap(),
    "fatGrams" to fatGrams.toFirestoreMap(),
    "fiberGrams" to fiberGrams.toFirestoreMap(),
    "folateMcg" to folateMcg.toFirestoreMap(),
    "ironMg" to ironMg.toFirestoreMap(),
    "calciumMg" to calciumMg.toFirestoreMap(),
    "vitaminDMcg" to vitaminDMcg.toFirestoreMap(),
    "vitaminB12Mcg" to vitaminB12Mcg.toFirestoreMap(),
    "iodineMcg" to iodineMcg.toFirestoreMap(),
    "omega3Mg" to omega3Mg.toFirestoreMap(),
    "cholineMg" to cholineMg.toFirestoreMap(),
    "waterMl" to waterMl.toFirestoreMap(),
)

private fun AiNutritionEstimate.toFirestoreMap(): Map<String, Any?> = mapOf(
    "value" to value,
    "confidence" to confidence,
    "explanation" to explanation,
    "source" to source,
)

private fun AiNutritionEstimates.toNutrientAmounts(): NutrientAmounts = NutrientAmounts(
    calories = caloriesKcal.value,
    proteinGrams = proteinGrams.value,
    fiberGrams = fiberGrams.value,
    folateMcg = folateMcg.value,
    ironMg = ironMg.value,
    calciumMg = calciumMg.value,
    vitaminDMcg = vitaminDMcg.value,
    vitaminB12Mcg = vitaminB12Mcg.value,
    iodineMcg = iodineMcg.value,
    omega3Mg = omega3Mg.value,
    cholineMg = cholineMg.value,
    waterMl = waterMl.value,
)

private fun NutrientAmounts.toFirestoreMap(): Map<String, Any> = mapOf(
    "calories" to calories,
    "proteinGrams" to proteinGrams,
    "fiberGrams" to fiberGrams,
    "folateMcg" to folateMcg,
    "ironMg" to ironMg,
    "calciumMg" to calciumMg,
    "vitaminDMcg" to vitaminDMcg,
    "vitaminB12Mcg" to vitaminB12Mcg,
    "iodineMcg" to iodineMcg,
    "omega3Mg" to omega3Mg,
    "cholineMg" to cholineMg,
    "waterMl" to waterMl,
)

private fun Map<*, *>.toAiNutritionEstimates(): AiNutritionEstimates = AiNutritionEstimates(
    caloriesKcal = (this["caloriesKcal"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    proteinGrams = (this["proteinGrams"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    carbsGrams = (this["carbsGrams"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    fatGrams = (this["fatGrams"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    fiberGrams = (this["fiberGrams"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    folateMcg = (this["folateMcg"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    ironMg = (this["ironMg"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    calciumMg = (this["calciumMg"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    vitaminDMcg = (this["vitaminDMcg"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    vitaminB12Mcg = (this["vitaminB12Mcg"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    iodineMcg = (this["iodineMcg"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    omega3Mg = (this["omega3Mg"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    cholineMg = (this["cholineMg"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
    waterMl = (this["waterMl"] as? Map<*, *>)?.toAiNutritionEstimate() ?: AiNutritionEstimate(),
)

private fun Map<*, *>.toAiNutritionEstimate(): AiNutritionEstimate = AiNutritionEstimate(
    value = number("value"),
    confidence = this["confidence"] as? String ?: "low",
    explanation = this["explanation"] as? String ?: DEFAULT_AI_ESTIMATE_EXPLANATION,
    source = this["source"] as? String ?: "ai",
)

private fun Map<*, *>.toAiNutritionGapGuidance(): AiNutritionGapGuidance = AiNutritionGapGuidance(
    nutrientKey = this["nutrientKey"] as? String ?: this["nutrient"] as? String ?: "unknown",
    displayName = this["displayName"] as? String ?: this["nutrient"] as? String ?: "Nutrient",
    status = this["status"] as? String ?: "unknown",
    explanation = this["explanation"] as? String ?: GENERIC_NUTRITION_GAP_EXPLANATION,
    foodSuggestions = getStringList("foodSuggestions"),
    safetyNote = this["safetyNote"] as? String ?: DEFAULT_NUTRITION_GAP_SAFETY_NOTE,
)

private fun AiSymptomGuidance.toFirestoreMap(): Map<String, Any?> = mapOf(
    "severity" to severity,
    "commonContext" to commonContext,
    "selfCare" to selfCare,
    "contactDoctorIf" to contactDoctorIf,
)

private fun AiWeightContext.toFirestoreMap(): Map<String, Any?> = mapOf(
    "summary" to summary,
    "doctorDiscussionRecommended" to doctorDiscussionRecommended,
)

private fun Map<*, *>.toAiSymptomGuidance(): AiSymptomGuidance = AiSymptomGuidance(
    severity = this["severity"] as? String ?: "none",
    commonContext = this["commonContext"] as? String ?: "",
    selfCare = getStringList("selfCare"),
    contactDoctorIf = getStringList("contactDoctorIf"),
)

private fun Map<*, *>.toAiWeightContext(): AiWeightContext = AiWeightContext(
    summary = this["summary"] as? String ?: "",
    doctorDiscussionRecommended = this["doctorDiscussionRecommended"] as? Boolean ?: false,
)

private fun DocumentSnapshot.toSymptomLog(): SymptomLog = SymptomLog(
    id = id,
    date = getString("date").orEmpty(),
    pregnancyProfileId = getString("pregnancyProfileId"),
    pregnancyWeek = getLong("pregnancyWeek")?.toInt(),
    trimester = getLong("trimester")?.toInt(),
    symptoms = (get("symptoms") as? List<*>)
        ?.mapNotNull { it as? Map<*, *> }
        ?.map { it.toSymptomEntry() }
        .orEmpty(),
    urgentFlag = getBoolean("urgentFlag") ?: false,
    urgentReasons = getStringList("urgentReasons"),
)

private fun Map<*, *>.toSymptomEntry(): SymptomEntry = SymptomEntry(
    name = this["name"] as? String ?: "Symptom",
    severity = (this["severity"] as? Number)?.toInt() ?: 1,
    duration = this["duration"] as? String ?: "",
    notes = this["notes"] as? String ?: "",
)

private fun DocumentSnapshot.toPregnancyProfile(): PregnancyProfile = PregnancyProfile(
    id = id,
    dateFoundOut = getString("dateFoundOut").orEmpty(),
    lastMenstrualPeriod = getString("lastMenstrualPeriod"),
    estimatedDueDate = getString("estimatedDueDate"),
    doctorConfirmedWeek = getLong("doctorConfirmedWeek")?.toInt(),
    pregnancyType = getString("pregnancyType").toPregnancyType(),
    heightCm = getDouble("heightCm"),
    prePregnancyWeightKg = getDouble("prePregnancyWeightKg"),
    currentWeightKg = getDouble("currentWeightKg") ?: 0.0,
    weightUnit = getString("weightUnit").toWeightUnit(),
    allergies = getStringList("allergies"),
    dietaryRestrictions = getStringList("dietaryRestrictions"),
    medicalConditions = getStringList("medicalConditions"),
    currentStatus = getString("currentStatus") ?: "active",
)

private fun DocumentSnapshot.getStringList(field: String): List<String> =
    (get(field) as? List<*>)?.mapNotNull { it as? String }.orEmpty()

private fun Map<*, *>.getStringList(field: String): List<String> =
    (this[field] as? List<*>)?.mapNotNull { it as? String }.orEmpty()

private fun Map<*, *>.number(key: String): Double = (this[key] as? Number)?.toDouble() ?: 0.0

private fun String?.toAiRequestType(): AiRequestType? = AiRequestType.entries.firstOrNull {
    it.wireValue == this
}

private fun String?.toAiNutritionEstimateSource(): AiNutritionEstimateSource = AiNutritionEstimateSource.entries.firstOrNull {
    it.wireValue == this
} ?: AiNutritionEstimateSource.LocalFallback

private fun String?.toPregnancyType(): PregnancyType = PregnancyType.entries.firstOrNull {
    it.firestoreValue == this
} ?: PregnancyType.Unknown

private fun String?.toWeightUnit(): WeightUnit = WeightUnit.entries.firstOrNull {
    it.firestoreValue == this
} ?: WeightUnit.Kg

private fun String.toLocalDateOrToday(): LocalDate = toLocalDateOrNull() ?: LocalDate.now()

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

private fun LocalDate.toWeekId(): String {
    val weekFields = WeekFields.ISO
    val weekBasedYear = get(weekFields.weekBasedYear())
    val week = get(weekFields.weekOfWeekBasedYear())
    return String.format(Locale.US, "%04d-W%02d", weekBasedYear, week)
}

private fun dailySummaryId(date: LocalDate): String = dailySummaryId(date.toString())

private fun dailySummaryId(date: String): String = "${AiRequestType.DailyNutritionSummary.wireValue}_$date".firestoreIdSafe()

private fun symptomSummaryId(symptomLogId: String): String =
    "${AiRequestType.SymptomExplanation.wireValue}_$symptomLogId".firestoreIdSafe()

private fun weeklySummaryId(weekId: String): String = "${AiRequestType.WeeklySummary.wireValue}_$weekId".firestoreIdSafe()

private fun String.firestoreIdSafe(): String = replace(Regex("[^A-Za-z0-9_-]"), "_").ifBlank { "ai_summary" }
