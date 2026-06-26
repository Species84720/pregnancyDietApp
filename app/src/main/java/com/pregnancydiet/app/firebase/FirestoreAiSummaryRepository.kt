package com.pregnancydiet.app.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.pregnancydiet.app.ai.AiNutritionGapGuidance
import com.pregnancydiet.app.ai.AiRepository
import com.pregnancydiet.app.ai.AiRequestBuilder
import com.pregnancydiet.app.ai.AiSummaryRecord
import com.pregnancydiet.app.ai.AiSummaryRecordFactory
import com.pregnancydiet.app.ai.AiSymptomGuidance
import com.pregnancydiet.app.ai.AiWeightContext
import com.pregnancydiet.app.ai.DefaultAiRepository
import com.pregnancydiet.app.data.AiSummaryRepository
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
        )
        userRef.collection(DAILY_NUTRITION_SUMMARIES_COLLECTION)
            .document(date.toString())
            .set(
                mapOf(
                    "aiSummary" to record.toFirestoreMap(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
        record
    }

    override suspend fun generateSymptomGuidance(
        uid: String,
        date: LocalDate,
    ): Result<AiSummaryRecord> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val context = loadActiveProfileContext(uid)
        val symptoms = loadSymptomLogsForDate(uid, date)
        val symptomLog = symptoms.firstOrNull()
            ?: error("Log a symptom for this date before generating symptom guidance.")
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
        )
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SYMPTOM_LOGS_COLLECTION)
            .document(symptomLog.id)
            .set(
                mapOf(
                    "aiSummary" to record.toFirestoreMap(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
        record
    }

    override suspend fun generateWeeklySummary(
        uid: String,
        endDate: LocalDate,
    ): Result<AiSummaryRecord> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val context = loadActiveProfileContext(uid)
        val weeklyTrend = nutritionRepository.loadWeeklyTrend(uid, endDate).getOrThrow()
        val weekId = endDate.toWeekId()
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
        )
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(WEEKLY_SUMMARIES_COLLECTION)
            .document(weekId)
            .set(
                mapOf(
                    "weekId" to weekId,
                    "pregnancyProfileId" to context.profile.id,
                    "pregnancyWeek" to context.progress.pregnancyWeek,
                    "trimester" to context.progress.trimester,
                    "daysIncluded" to weeklyTrend.daysIncluded,
                    "repeatedGaps" to weeklyTrend.repeatedGaps,
                    "aiSummary" to record.toFirestoreMap(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
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

private fun AiSummaryRecord.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "type" to requestType.wireValue,
    "date" to date,
    "weekId" to weekId,
    "pregnancyProfileId" to pregnancyProfileId,
    "inputContextVersion" to inputContextVersion,
    "summary" to summary,
    "stageContext" to stageContext,
    "nutritionGaps" to nutritionGaps.map { it.toFirestoreMap() },
    "symptomGuidance" to symptomGuidance?.toFirestoreMap(),
    "weightContext" to weightContext?.toFirestoreMap(),
    "urgentWarning" to urgentWarning,
    "urgentReasons" to urgentReasons,
    "nextSteps" to nextSteps,
    "disclaimer" to disclaimer,
    "fallback" to fallback,
    "fallbackReason" to fallbackReason,
    "createdAtIso" to createdAtIso,
)

private fun AiNutritionGapGuidance.toFirestoreMap(): Map<String, Any?> = mapOf(
    "nutrient" to nutrient,
    "status" to status,
    "explanation" to explanation,
    "foodSuggestions" to foodSuggestions,
    "safetyNote" to safetyNote,
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
