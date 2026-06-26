package com.pregnancydiet.app.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.pregnancydiet.app.data.ReportsRepository
import com.pregnancydiet.app.model.DailyNutritionSummary
import com.pregnancydiet.app.model.FoodNutrition
import com.pregnancydiet.app.model.GapSeverity
import com.pregnancydiet.app.model.MealFoodItem
import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.model.MealType
import com.pregnancydiet.app.model.NutrientAmounts
import com.pregnancydiet.app.model.NutritionGap
import com.pregnancydiet.app.model.NutritionStatus
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.Supplement
import com.pregnancydiet.app.model.SupplementLog
import com.pregnancydiet.app.model.SymptomEntry
import com.pregnancydiet.app.model.SymptomLog
import com.pregnancydiet.app.model.WeightLog
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.pregnancy.PregnancyCalculator
import com.pregnancydiet.app.pregnancy.PregnancyDatingInput
import com.pregnancydiet.app.reports.GynecologistReport
import com.pregnancydiet.app.reports.ReportDateRange
import com.pregnancydiet.app.reports.ReportWeeklyAiSummary
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.WeekFields
import java.util.Locale

class FirestoreReportsRepository(
    private val pregnancyCalculator: PregnancyCalculator = PregnancyCalculator(),
) : ReportsRepository {
    override suspend fun loadReport(
        uid: String,
        dateRange: ReportDateRange,
    ): Result<GynecologistReport> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val userSnapshot = userRef.get().await()
        val activePregnancyProfileId = userSnapshot.getString("activePregnancyProfileId")?.takeIf { it.isNotBlank() }
        val profile = activePregnancyProfileId?.let { profileId ->
            userRef.collection(PREGNANCY_PROFILES_COLLECTION)
                .document(profileId)
                .get()
                .await()
                .takeIf { it.exists() }
                ?.toPregnancyProfile()
        }
        val progress = profile?.let {
            pregnancyCalculator.calculate(
                PregnancyDatingInput(
                    dateFoundOut = it.dateFoundOut.toLocalDateOrToday(),
                    lastMenstrualPeriod = it.lastMenstrualPeriod?.toLocalDateOrNull(),
                    estimatedDueDate = it.estimatedDueDate?.toLocalDateOrNull(),
                    doctorConfirmedWeek = it.doctorConfirmedWeek,
                ),
            )
        }

        GynecologistReport(
            generatedAt = LocalDateTime.now(),
            dateRange = dateRange,
            pregnancyProfile = profile,
            pregnancyProgress = progress,
            symptomLogs = loadSymptoms(uid, dateRange),
            supplements = loadSupplements(uid),
            supplementLogs = loadSupplementLogs(uid, dateRange),
            mealLogs = loadMeals(uid, dateRange),
            nutritionSummaries = loadNutritionSummaries(uid, dateRange),
            weightLogs = loadWeightLogs(uid, dateRange),
            weeklyAiSummaries = loadWeeklyAiSummaries(uid, dateRange),
        )
    }

    private suspend fun loadSymptoms(uid: String, dateRange: ReportDateRange): List<SymptomLog> {
        val firestore = FirebaseConfiguration.firestoreOrNull() ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SYMPTOM_LOGS_COLLECTION)
            .whereGreaterThanOrEqualTo("date", dateRange.startDate.toString())
            .whereLessThanOrEqualTo("date", dateRange.endDate.toString())
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .map { it.toSymptomLog() }
    }

    private suspend fun loadSupplements(uid: String): List<Supplement> {
        val firestore = FirebaseConfiguration.firestoreOrNull() ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SUPPLEMENTS_COLLECTION)
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .map { it.toSupplement() }
    }

    private suspend fun loadSupplementLogs(uid: String, dateRange: ReportDateRange): List<SupplementLog> {
        val firestore = FirebaseConfiguration.firestoreOrNull() ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SUPPLEMENT_LOGS_COLLECTION)
            .whereGreaterThanOrEqualTo("date", dateRange.startDate.toString())
            .whereLessThanOrEqualTo("date", dateRange.endDate.toString())
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .map { it.toSupplementLog() }
    }

    private suspend fun loadMeals(uid: String, dateRange: ReportDateRange): List<MealLog> {
        val firestore = FirebaseConfiguration.firestoreOrNull() ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(MEAL_LOGS_COLLECTION)
            .whereGreaterThanOrEqualTo("date", dateRange.startDate.toString())
            .whereLessThanOrEqualTo("date", dateRange.endDate.toString())
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .map { it.toMealLog() }
    }

    private suspend fun loadNutritionSummaries(uid: String, dateRange: ReportDateRange): List<DailyNutritionSummary> {
        val firestore = FirebaseConfiguration.firestoreOrNull() ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(DAILY_NUTRITION_SUMMARIES_COLLECTION)
            .whereGreaterThanOrEqualTo("date", dateRange.startDate.toString())
            .whereLessThanOrEqualTo("date", dateRange.endDate.toString())
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .map { it.toDailyNutritionSummary() }
    }

    private suspend fun loadWeightLogs(uid: String, dateRange: ReportDateRange): List<WeightLog> {
        val firestore = FirebaseConfiguration.firestoreOrNull() ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(WEIGHT_LOGS_COLLECTION)
            .whereGreaterThanOrEqualTo("date", dateRange.startDate.toString())
            .whereLessThanOrEqualTo("date", dateRange.endDate.toString())
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .map { it.toWeightLog() }
    }

    private suspend fun loadWeeklyAiSummaries(uid: String, dateRange: ReportDateRange): List<ReportWeeklyAiSummary> {
        val firestore = FirebaseConfiguration.firestoreOrNull() ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(WEEKLY_SUMMARIES_COLLECTION)
            .whereGreaterThanOrEqualTo("weekId", dateRange.startDate.toWeekId())
            .whereLessThanOrEqualTo("weekId", dateRange.endDate.toWeekId())
            .orderBy("weekId", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .map { it.toReportWeeklyAiSummary() }
    }
}

private const val USERS_COLLECTION = "users"
private const val PREGNANCY_PROFILES_COLLECTION = "pregnancyProfiles"
private const val SYMPTOM_LOGS_COLLECTION = "symptomLogs"
private const val SUPPLEMENTS_COLLECTION = "supplements"
private const val SUPPLEMENT_LOGS_COLLECTION = "supplementLogs"
private const val MEAL_LOGS_COLLECTION = "mealLogs"
private const val DAILY_NUTRITION_SUMMARIES_COLLECTION = "dailyNutritionSummaries"
private const val WEIGHT_LOGS_COLLECTION = "weightLogs"
private const val WEEKLY_SUMMARIES_COLLECTION = "weeklySummaries"

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

private fun DocumentSnapshot.toSupplement(): Supplement = Supplement(
    id = id,
    name = getString("name").orEmpty(),
    dose = getString("dose").orEmpty(),
    frequency = getString("frequency").orEmpty(),
    timeOfDay = getString("timeOfDay").orEmpty(),
    prescribedBy = getString("prescribedBy").orEmpty(),
    instructions = getString("instructions").orEmpty(),
    startDate = getString("startDate").orEmpty(),
    endDate = getString("endDate"),
    active = getBoolean("active") ?: true,
)

private fun DocumentSnapshot.toSupplementLog(): SupplementLog = SupplementLog(
    id = id,
    supplementId = getString("supplementId").orEmpty(),
    date = getString("date").orEmpty(),
    taken = getBoolean("taken") ?: false,
    takenAt = getString("takenAt"),
    notes = getString("notes").orEmpty(),
)

private fun DocumentSnapshot.toMealLog(): MealLog = MealLog(
    id = id,
    date = getString("date").orEmpty(),
    pregnancyProfileId = getString("pregnancyProfileId"),
    pregnancyWeek = getLong("pregnancyWeek")?.toInt(),
    trimester = getLong("trimester")?.toInt(),
    mealType = getString("mealType").toMealType(),
    items = (get("items") as? List<*>)
        ?.mapNotNull { it as? Map<*, *> }
        ?.map { it.toMealFoodItem() }
        .orEmpty(),
)

private fun Map<*, *>.toMealFoodItem(): MealFoodItem = MealFoodItem(
    foodName = this["foodName"] as? String ?: "Food",
    quantity = (this["quantity"] as? Number)?.toDouble() ?: 1.0,
    unit = this["unit"] as? String ?: "serving",
    weightGrams = (this["weightGrams"] as? Number)?.toDouble(),
    nutrition = (this["nutrition"] as? Map<*, *>)?.toFoodNutrition() ?: FoodNutrition(),
)

private fun Map<*, *>.toFoodNutrition(): FoodNutrition = FoodNutrition(
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

private fun DocumentSnapshot.toWeightLog(): WeightLog = WeightLog(
    id = id,
    date = getString("date").orEmpty(),
    pregnancyProfileId = getString("pregnancyProfileId").orEmpty(),
    pregnancyWeek = getLong("pregnancyWeek")?.toInt(),
    weightKg = getDouble("weightKg") ?: 0.0,
    source = getString("source") ?: "manual",
    notes = getString("notes") ?: "",
)

private fun DocumentSnapshot.toReportWeeklyAiSummary(): ReportWeeklyAiSummary {
    val aiSummary = get("aiSummary") as? Map<*, *>
    return ReportWeeklyAiSummary(
        weekId = getString("weekId").ifBlankOrNull { id },
        pregnancyWeek = getLong("pregnancyWeek")?.toInt(),
        trimester = getLong("trimester")?.toInt(),
        summary = aiSummary?.get("summary") as? String ?: "No summary text saved.",
        urgentWarning = aiSummary?.get("urgentWarning") as? Boolean ?: false,
        fallback = aiSummary?.get("fallback") as? Boolean ?: false,
    )
}

private fun DocumentSnapshot.getStringList(field: String): List<String> =
    (get(field) as? List<*>)?.mapNotNull { it as? String }.orEmpty()

private fun Map<*, *>.number(key: String): Double = (this[key] as? Number)?.toDouble() ?: 0.0

private fun String?.toPregnancyType(): PregnancyType = PregnancyType.entries.firstOrNull {
    it.firestoreValue == this
} ?: PregnancyType.Unknown

private fun String?.toWeightUnit(): WeightUnit = WeightUnit.entries.firstOrNull {
    it.firestoreValue == this
} ?: WeightUnit.Kg

private fun String?.toMealType(): MealType = MealType.entries.firstOrNull {
    it.firestoreValue == this
} ?: MealType.Breakfast

private fun String?.toNutritionStatus(): NutritionStatus = NutritionStatus.entries.firstOrNull {
    it.firestoreValue == this
} ?: NutritionStatus.Low

private fun String?.toGapSeverity(): GapSeverity = GapSeverity.entries.firstOrNull {
    it.firestoreValue == this
} ?: GapSeverity.Mild

private fun String.toLocalDateOrToday(): LocalDate = toLocalDateOrNull() ?: LocalDate.now()

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

private fun LocalDate.toWeekId(): String {
    val weekFields = WeekFields.ISO
    val weekBasedYear = get(weekFields.weekBasedYear())
    val week = get(weekFields.weekOfWeekBasedYear())
    return String.format(Locale.US, "%04d-W%02d", weekBasedYear, week)
}

private inline fun String?.ifBlankOrNull(defaultValue: () -> String): String =
    if (this.isNullOrBlank()) defaultValue() else this
