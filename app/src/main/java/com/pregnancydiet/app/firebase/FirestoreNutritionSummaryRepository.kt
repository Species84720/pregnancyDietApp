package com.pregnancydiet.app.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.pregnancydiet.app.data.NutritionSummaryRepository
import com.pregnancydiet.app.model.DailyNutritionSummary
import com.pregnancydiet.app.model.GapSeverity
import com.pregnancydiet.app.model.NutrientAmounts
import com.pregnancydiet.app.model.NutritionGap
import com.pregnancydiet.app.model.NutritionStatus
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.model.WeeklyNutritionTrend
import com.pregnancydiet.app.nutrition.NutritionSummaryCalculator
import com.pregnancydiet.app.pregnancy.PregnancyCalculator
import com.pregnancydiet.app.pregnancy.PregnancyDatingInput
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class FirestoreNutritionSummaryRepository(
    private val pregnancyCalculator: PregnancyCalculator = PregnancyCalculator(),
) : NutritionSummaryRepository {
    override suspend fun generateAndSaveDailySummary(
        uid: String,
        date: LocalDate,
    ): Result<DailyNutritionSummary> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val context = loadProfileContext(uid).getOrThrow()
        val meals = FirestoreMealLogRepository().loadMealLogsForDate(uid, date).getOrThrow()
        val summary = NutritionSummaryCalculator.dailySummary(
            date = date.toString(),
            pregnancyProfile = context.profile,
            pregnancyWeek = context.pregnancyWeek,
            trimester = context.trimester,
            meals = meals,
        )
        userRef.collection(DAILY_NUTRITION_SUMMARIES_COLLECTION)
            .document(date.toString())
            .set(summary.toFirestoreMap())
            .await()
        summary
    }

    override suspend fun loadDailySummary(uid: String, date: LocalDate): Result<DailyNutritionSummary?> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(DAILY_NUTRITION_SUMMARIES_COLLECTION)
            .document(date.toString())
            .get()
            .await()
            .takeIf { it.exists() }
            ?.toDailyNutritionSummary()
    }

    override suspend fun loadWeeklyTrend(uid: String, endDate: LocalDate): Result<WeeklyNutritionTrend> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val summariesRef = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(DAILY_NUTRITION_SUMMARIES_COLLECTION)
        val summaries = (0L..6L).mapNotNull { daysBack ->
            val date = endDate.minusDays(daysBack).toString()
            summariesRef.document(date).get().await().takeIf { it.exists() }?.toDailyNutritionSummary()
        }.sortedBy { it.date }
        NutritionSummaryCalculator.weeklyTrend(summaries)
    }

    private suspend fun loadProfileContext(uid: String): Result<NutritionProfileContext> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val userSnapshot = userRef.get().await()
        val activePregnancyProfileId = userSnapshot.getString("activePregnancyProfileId")
            ?.takeIf { it.isNotBlank() }
            ?: error("Complete onboarding before generating nutrition summaries.")
        val profileSnapshot = userRef.collection(PREGNANCY_PROFILES_COLLECTION)
            .document(activePregnancyProfileId)
            .get()
            .await()
        if (!profileSnapshot.exists()) error("Complete onboarding before generating nutrition summaries.")
        val profile = profileSnapshot.toPregnancyProfile()
        val progress = pregnancyCalculator.calculate(
            PregnancyDatingInput(
                dateFoundOut = profile.dateFoundOut.toLocalDateOrToday(),
                lastMenstrualPeriod = profile.lastMenstrualPeriod?.toLocalDateOrNull(),
                estimatedDueDate = profile.estimatedDueDate?.toLocalDateOrNull(),
                doctorConfirmedWeek = profile.doctorConfirmedWeek,
            ),
        )
        NutritionProfileContext(
            profile = profile,
            pregnancyWeek = progress.pregnancyWeek,
            trimester = progress.trimester,
        )
    }
}

private data class NutritionProfileContext(
    val profile: PregnancyProfile,
    val pregnancyWeek: Int?,
    val trimester: Int?,
)

private const val USERS_COLLECTION = "users"
private const val PREGNANCY_PROFILES_COLLECTION = "pregnancyProfiles"
private const val DAILY_NUTRITION_SUMMARIES_COLLECTION = "dailyNutritionSummaries"

private fun DailyNutritionSummary.toFirestoreMap(): Map<String, Any?> = mapOf(
    "date" to date,
    "pregnancyProfileId" to pregnancyProfileId,
    "pregnancyWeek" to pregnancyWeek,
    "trimester" to trimester,
    "currentWeightKg" to currentWeightKg,
    "nutritionProfileVersion" to nutritionProfileVersion,
    "totals" to totals.toFirestoreMap(),
    "targets" to targets.toFirestoreMap(),
    "gaps" to gaps.map { it.toFirestoreMap() },
    "stagePriorities" to stagePriorities,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp(),
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

private fun NutritionGap.toFirestoreMap(): Map<String, Any> = mapOf(
    "nutrient" to nutrient,
    "label" to label,
    "status" to status.firestoreValue,
    "severity" to severity.firestoreValue,
    "total" to total,
    "target" to target,
    "unit" to unit,
    "foodSuggestion" to foodSuggestion,
)

private fun DocumentSnapshot.toDailyNutritionSummary(): DailyNutritionSummary = DailyNutritionSummary(
    date = getString("date").orEmpty(),
    pregnancyProfileId = getString("pregnancyProfileId"),
    pregnancyWeek = getLong("pregnancyWeek")?.toInt(),
    trimester = getLong("trimester")?.toInt(),
    currentWeightKg = getDouble("currentWeightKg") ?: 0.0,
    nutritionProfileVersion = getString("nutritionProfileVersion").orEmpty(),
    totals = (get("totals") as? Map<*, *>)?.toNutrientAmounts() ?: NutrientAmounts(),
    targets = (get("targets") as? Map<*, *>)?.toNutrientAmounts() ?: NutrientAmounts(),
    gaps = (get("gaps") as? List<*>)
        ?.mapNotNull { it as? Map<*, *> }
        ?.map { it.toNutritionGap() }
        .orEmpty(),
    stagePriorities = getStringList("stagePriorities"),
)

private fun Map<*, *>.toNutrientAmounts(): NutrientAmounts = NutrientAmounts(
    calories = number("calories"),
    proteinGrams = number("proteinGrams"),
    fiberGrams = number("fiberGrams"),
    folateMcg = number("folateMcg"),
    ironMg = number("ironMg"),
    calciumMg = number("calciumMg"),
    vitaminDMcg = number("vitaminDMcg"),
    vitaminB12Mcg = number("vitaminB12Mcg"),
    iodineMcg = number("iodineMcg"),
    omega3Mg = number("omega3Mg"),
    cholineMg = number("cholineMg"),
    waterMl = number("waterMl"),
)

private fun Map<*, *>.toNutritionGap(): NutritionGap = NutritionGap(
    nutrient = this["nutrient"] as? String ?: "unknown",
    label = this["label"] as? String ?: "Nutrient",
    status = (this["status"] as? String).toNutritionStatus(),
    severity = (this["severity"] as? String).toGapSeverity(),
    total = number("total"),
    target = number("target"),
    unit = this["unit"] as? String ?: "",
    foodSuggestion = this["foodSuggestion"] as? String ?: "Prefer food-based options that fit your restrictions.",
)

private fun Map<*, *>.number(key: String): Double = (this[key] as? Number)?.toDouble() ?: 0.0

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

private fun String?.toNutritionStatus(): NutritionStatus = NutritionStatus.entries.firstOrNull {
    it.firestoreValue == this
} ?: NutritionStatus.Low

private fun String?.toGapSeverity(): GapSeverity = GapSeverity.entries.firstOrNull {
    it.firestoreValue == this
} ?: GapSeverity.Mild

private fun String.toLocalDateOrToday(): LocalDate = toLocalDateOrNull() ?: LocalDate.now()

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()
