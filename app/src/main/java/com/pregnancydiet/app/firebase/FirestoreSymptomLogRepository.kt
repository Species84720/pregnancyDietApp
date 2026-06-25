package com.pregnancydiet.app.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.pregnancydiet.app.data.SymptomLogRepository
import com.pregnancydiet.app.data.SymptomLoggingContext
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.SymptomEntry
import com.pregnancydiet.app.model.SymptomLog
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.pregnancy.PregnancyCalculator
import com.pregnancydiet.app.pregnancy.PregnancyDatingInput
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class FirestoreSymptomLogRepository(
    private val pregnancyCalculator: PregnancyCalculator = PregnancyCalculator(),
) : SymptomLogRepository {
    override suspend fun loadLoggingContext(uid: String): Result<SymptomLoggingContext> = runCatching {
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
        SymptomLoggingContext(
            pregnancyProfileId = activePregnancyProfileId,
            progress = progress,
        )
    }

    override suspend fun saveSymptomLog(uid: String, symptomLog: SymptomLog): Result<SymptomLog> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val logRef = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SYMPTOM_LOGS_COLLECTION)
            .document()
        val logWithId = symptomLog.copy(id = logRef.id)
        logRef.set(logWithId.toFirestoreMap()).await()
        logWithId
    }

    override suspend fun loadRecentSymptomLogs(uid: String, limit: Long): Result<List<SymptomLog>> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SYMPTOM_LOGS_COLLECTION)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
            .documents
            .map { it.toSymptomLog() }
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
private const val SYMPTOM_LOGS_COLLECTION = "symptomLogs"

private fun SymptomLog.toFirestoreMap(): Map<String, Any?> = mapOf(
    "date" to date,
    "pregnancyProfileId" to pregnancyProfileId,
    "pregnancyWeek" to pregnancyWeek,
    "trimester" to trimester,
    "symptoms" to symptoms.map { it.toFirestoreMap() },
    "urgentFlag" to urgentFlag,
    "urgentReasons" to urgentReasons,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp(),
)

private fun SymptomEntry.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "severity" to severity,
    "duration" to duration,
    "notes" to notes,
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
