package com.pregnancydiet.app.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.pregnancydiet.app.data.MealLogRepository
import com.pregnancydiet.app.data.MealLoggingContext
import com.pregnancydiet.app.model.FoodNutrition
import com.pregnancydiet.app.model.MealFoodItem
import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.model.MealType
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.pregnancy.PregnancyCalculator
import com.pregnancydiet.app.pregnancy.PregnancyDatingInput
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class FirestoreMealLogRepository(
    private val pregnancyCalculator: PregnancyCalculator = PregnancyCalculator(),
) : MealLogRepository {
    override suspend fun loadLoggingContext(uid: String): Result<MealLoggingContext> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val userSnapshot = userRef.get().await()
        val activePregnancyProfileId = userSnapshot.getString("activePregnancyProfileId")
            ?.takeIf { it.isNotBlank() }
        val profile = activePregnancyProfileId?.let { profileId ->
            userRef.collection(PREGNANCY_PROFILES_COLLECTION)
                .document(profileId)
                .get()
                .await()
                .takeIf { it.exists() }
                ?.toPregnancyProfile()
        }
        val progress = profile?.toPregnancyProgress() ?: pregnancyCalculator.calculate(
            PregnancyDatingInput(
                dateFoundOut = LocalDate.now(),
                lastMenstrualPeriod = null,
                estimatedDueDate = null,
                doctorConfirmedWeek = null,
            ),
        )
        MealLoggingContext(
            pregnancyProfileId = activePregnancyProfileId,
            progress = progress,
        )
    }

    override suspend fun loadMealLogsForDate(uid: String, date: LocalDate): Result<List<MealLog>> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(MEAL_LOGS_COLLECTION)
            .whereEqualTo("date", date.toString())
            .get()
            .await()
            .documents
            .map { it.toMealLog() }
            .sortedWith(compareBy<MealLog> { it.mealType.ordinal }.thenBy { it.id })
    }

    override suspend fun saveMealLog(uid: String, mealLog: MealLog): Result<MealLog> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val logsRef = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(MEAL_LOGS_COLLECTION)
        val logRef = mealLog.id.takeIf { it.isNotBlank() }
            ?.let(logsRef::document)
            ?: logsRef.document()
        val logWithId = mealLog.copy(id = logRef.id)
        logRef.set(logWithId.toFirestoreMap()).await()
        logWithId
    }

    override suspend fun deleteMealLog(uid: String, mealId: String): Result<Unit> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(MEAL_LOGS_COLLECTION)
            .document(mealId)
            .delete()
            .await()
    }

    private fun PregnancyProfile.toPregnancyProgress() = pregnancyCalculator.calculate(
        PregnancyDatingInput(
            dateFoundOut = dateFoundOut.toLocalDateOrToday(),
            lastMenstrualPeriod = lastMenstrualPeriod?.toLocalDateOrNull(),
            estimatedDueDate = estimatedDueDate?.toLocalDateOrNull(),
            doctorConfirmedWeek = doctorConfirmedWeek,
        ),
    )
}

private const val USERS_COLLECTION = "users"
private const val PREGNANCY_PROFILES_COLLECTION = "pregnancyProfiles"
private const val MEAL_LOGS_COLLECTION = "mealLogs"

private fun MealLog.toFirestoreMap(): Map<String, Any?> = mapOf(
    "date" to date,
    "pregnancyProfileId" to pregnancyProfileId,
    "pregnancyWeek" to pregnancyWeek,
    "trimester" to trimester,
    "mealType" to mealType.firestoreValue,
    "items" to items.map { it.toFirestoreMap() },
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp(),
)

private fun MealFoodItem.toFirestoreMap(): Map<String, Any?> = mapOf(
    "foodName" to foodName,
    "quantity" to quantity,
    "unit" to unit,
    "weightGrams" to weightGrams,
    "nutrition" to nutrition.toFirestoreMap(),
)

private fun FoodNutrition.toFirestoreMap(): Map<String, Any> = mapOf(
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

private fun Map<*, *>.number(key: String): Double = (this[key] as? Number)?.toDouble() ?: 0.0

private fun String?.toMealType(): MealType = MealType.entries.firstOrNull {
    it.firestoreValue == this
} ?: MealType.Breakfast

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
