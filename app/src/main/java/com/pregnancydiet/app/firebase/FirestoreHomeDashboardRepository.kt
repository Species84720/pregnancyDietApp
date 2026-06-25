package com.pregnancydiet.app.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.pregnancydiet.app.data.HomeDashboardRepository
import com.pregnancydiet.app.home.HomeDashboard
import com.pregnancydiet.app.home.HomeDashboardMapper
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.pregnancy.PregnancyCalculator
import com.pregnancydiet.app.pregnancy.PregnancyDatingInput
import com.pregnancydiet.app.supplements.SupplementStatusMapper
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class FirestoreHomeDashboardRepository(
    private val pregnancyCalculator: PregnancyCalculator = PregnancyCalculator(),
) : HomeDashboardRepository {
    override suspend fun loadHomeDashboard(uid: String): Result<HomeDashboard?> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val userSnapshot = userRef.get().await()
        val activePregnancyProfileId = userSnapshot.getString("activePregnancyProfileId")
            ?.takeIf { it.isNotBlank() }
            ?: return@runCatching null

        val profileSnapshot = userRef
            .collection(PREGNANCY_PROFILES_COLLECTION)
            .document(activePregnancyProfileId)
            .get()
            .await()

        if (!profileSnapshot.exists()) return@runCatching null

        val profile = profileSnapshot.toPregnancyProfile()
        val progress = pregnancyCalculator.calculate(
            input = PregnancyDatingInput(
                dateFoundOut = profile.dateFoundOut.toLocalDateOrToday(),
                lastMenstrualPeriod = profile.lastMenstrualPeriod?.toLocalDateOrNull(),
                estimatedDueDate = profile.estimatedDueDate?.toLocalDateOrNull(),
                doctorConfirmedWeek = profile.doctorConfirmedWeek,
            ),
        )
        val today = LocalDate.now()
        val supplementStatus = FirestoreSupplementRepository()
            .loadSupplementsForDate(uid = uid, date = today)
            .getOrNull()
            ?.let(SupplementStatusMapper::todayStatus)
            ?: "No supplement status yet."

        HomeDashboardMapper.createDashboard(
            pregnancyProfile = profile,
            progress = progress,
            today = today,
            todaySupplementStatus = supplementStatus,
        )
    }
}

private const val USERS_COLLECTION = "users"
private const val PREGNANCY_PROFILES_COLLECTION = "pregnancyProfiles"

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