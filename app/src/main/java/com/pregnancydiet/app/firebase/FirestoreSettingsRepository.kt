package com.pregnancydiet.app.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pregnancydiet.app.data.SettingsData
import com.pregnancydiet.app.data.SettingsRepository
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.PrivacySettings
import com.pregnancydiet.app.model.UserProfile
import com.pregnancydiet.app.model.WeightLog
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.pregnancy.PregnancyCalculator
import com.pregnancydiet.app.pregnancy.PregnancyDatingInput
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.math.abs

class FirestoreSettingsRepository(
    private val pregnancyCalculator: PregnancyCalculator = PregnancyCalculator(),
) : SettingsRepository {
    override suspend fun loadSettings(uid: String): Result<SettingsData> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val userSnapshot = userRef.get().await()
        val userProfile = userSnapshot.takeIf { it.exists() }?.toUserProfile()
        val activePregnancyProfileId = userSnapshot.getString("activePregnancyProfileId")
            ?.takeIf { it.isNotBlank() }
        val pregnancyProfile = activePregnancyProfileId?.let { profileId ->
            userRef.collection(PREGNANCY_PROFILES_COLLECTION)
                .document(profileId)
                .get()
                .await()
                .takeIf { it.exists() }
                ?.toPregnancyProfile()
        }
        val privacySettings = userRef.collection(PRIVACY_SETTINGS_COLLECTION)
            .document(DEFAULT_DOCUMENT_ID)
            .get()
            .await()
            .toPrivacySettings()

        SettingsData(
            userProfile = userProfile,
            pregnancyProfile = pregnancyProfile,
            privacySettings = privacySettings,
        )
    }

    override suspend fun savePregnancyProfile(
        uid: String,
        pregnancyProfile: PregnancyProfile,
    ): Result<PregnancyProfile> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        require(pregnancyProfile.id.isNotBlank()) { "Complete onboarding before editing pregnancy settings." }

        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val profileRef = userRef.collection(PREGNANCY_PROFILES_COLLECTION).document(pregnancyProfile.id)
        val existingProfile = profileRef.get().await().takeIf { it.exists() }?.toPregnancyProfile()
        val progress = pregnancyCalculator.calculate(
            PregnancyDatingInput(
                dateFoundOut = pregnancyProfile.dateFoundOut.toLocalDateOrToday(),
                lastMenstrualPeriod = pregnancyProfile.lastMenstrualPeriod?.toLocalDateOrNull(),
                estimatedDueDate = pregnancyProfile.estimatedDueDate?.toLocalDateOrNull(),
                doctorConfirmedWeek = pregnancyProfile.doctorConfirmedWeek,
            ),
        )
        val weightChanged = existingProfile?.let {
            abs(it.currentWeightKg - pregnancyProfile.currentWeightKg) >= WEIGHT_CHANGE_THRESHOLD_KG
        } ?: true
        val weightLogRef = if (weightChanged) userRef.collection(WEIGHT_LOGS_COLLECTION).document() else null
        val weightLog = weightLogRef?.let {
            WeightLog(
                id = it.id,
                date = LocalDate.now().toString(),
                pregnancyProfileId = pregnancyProfile.id,
                pregnancyWeek = progress.pregnancyWeek,
                weightKg = pregnancyProfile.currentWeightKg,
                source = "settings",
                notes = "Updated from Settings",
            )
        }

        firestore.runBatch { batch ->
            batch.set(profileRef, pregnancyProfile.toFirestoreMap(progress.trimester, progress.pregnancyWeek, progress.dayWithinWeek, progress.estimatedDueDate?.toString()), SetOptions.merge())
            weightLogRef?.let { ref -> weightLog?.let { batch.set(ref, it.toFirestoreMap()) } }
            batch.set(
                userRef,
                mapOf(
                    "activePregnancyProfileId" to pregnancyProfile.id,
                    "onboardingCompleted" to true,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
        }.await()

        pregnancyProfile
    }

    override suspend fun savePrivacySettings(
        uid: String,
        privacySettings: PrivacySettings,
    ): Result<PrivacySettings> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val updated = privacySettings.copy(updatedAtIso = OffsetDateTime.now().toString())
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(PRIVACY_SETTINGS_COLLECTION)
            .document(DEFAULT_DOCUMENT_ID)
            .set(updated.toFirestoreMap(), SetOptions.merge())
            .await()
        updated
    }

    override suspend fun deleteUserData(uid: String): Result<Unit> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        USER_SUBCOLLECTIONS.forEach { collectionName ->
            deleteSubcollection(
                firestore = firestore,
                uid = uid,
                collectionName = collectionName,
            )
        }
        userRef.delete().await()
    }
}

private const val USERS_COLLECTION = "users"
private const val PREGNANCY_PROFILES_COLLECTION = "pregnancyProfiles"
private const val WEIGHT_LOGS_COLLECTION = "weightLogs"
private const val PRIVACY_SETTINGS_COLLECTION = "privacySettings"
private const val DEFAULT_DOCUMENT_ID = "default"
private const val BATCH_DELETE_LIMIT = 450L
private const val WEIGHT_CHANGE_THRESHOLD_KG = 0.01

private val USER_SUBCOLLECTIONS = listOf(
    "pregnancyProfiles",
    "weightLogs",
    "symptomLogs",
    "supplements",
    "supplementLogs",
    "mealLogs",
    "dailyNutritionSummaries",
    "weeklySummaries",
    "weeklyNutritionSummaries",
    "aiSummaries",
    "reminderPreferences",
    "privacySettings",
)

private suspend fun deleteSubcollection(
    firestore: FirebaseFirestore,
    uid: String,
    collectionName: String,
) {
    val collectionRef = firestore.collection(USERS_COLLECTION)
        .document(uid)
        .collection(collectionName)

    while (true) {
        val snapshot = collectionRef.limit(BATCH_DELETE_LIMIT).get().await()
        if (snapshot.documents.isEmpty()) return

        val batch = firestore.batch()
        snapshot.documents.forEach { document -> batch.delete(document.reference) }
        batch.commit().await()

        if (snapshot.size() < BATCH_DELETE_LIMIT) return
    }
}

private fun DocumentSnapshot.toUserProfile(): UserProfile = UserProfile(
    uid = getString("uid") ?: id,
    email = getString("email"),
    displayName = getString("displayName"),
    photoUrl = getString("photoUrl"),
    onboardingCompleted = getBoolean("onboardingCompleted") ?: false,
    activePregnancyProfileId = getString("activePregnancyProfileId"),
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

private fun PregnancyProfile.toFirestoreMap(
    trimester: Int?,
    pregnancyWeek: Int?,
    dayWithinWeek: Int?,
    calculatedEstimatedDueDate: String?,
): Map<String, Any?> = mapOf(
    "dateFoundOut" to dateFoundOut,
    "lastMenstrualPeriod" to lastMenstrualPeriod,
    "estimatedDueDate" to estimatedDueDate,
    "calculatedEstimatedDueDate" to calculatedEstimatedDueDate,
    "doctorConfirmedWeek" to doctorConfirmedWeek,
    "pregnancyType" to pregnancyType.firestoreValue,
    "currentStatus" to currentStatus,
    "heightCm" to heightCm,
    "prePregnancyWeightKg" to prePregnancyWeightKg,
    "currentWeightKg" to currentWeightKg,
    "weightUnit" to weightUnit.firestoreValue,
    "allergies" to allergies,
    "dietaryRestrictions" to dietaryRestrictions,
    "medicalConditions" to medicalConditions,
    "pregnancyWeek" to pregnancyWeek,
    "dayWithinWeek" to dayWithinWeek,
    "trimester" to trimester,
    "updatedAt" to FieldValue.serverTimestamp(),
)

private fun WeightLog.toFirestoreMap(): Map<String, Any?> = mapOf(
    "date" to date,
    "pregnancyProfileId" to pregnancyProfileId,
    "pregnancyWeek" to pregnancyWeek,
    "weightKg" to weightKg,
    "source" to source,
    "notes" to notes,
    "createdAt" to FieldValue.serverTimestamp(),
)

private fun DocumentSnapshot.toPrivacySettings(): PrivacySettings = PrivacySettings(
    aiProcessingAllowed = getBoolean("aiProcessingAllowed") ?: true,
    updatedAtIso = getString("updatedAtIso"),
)

private fun PrivacySettings.toFirestoreMap(): Map<String, Any?> = mapOf(
    "aiProcessingAllowed" to aiProcessingAllowed,
    "updatedAtIso" to updatedAtIso,
    "updatedAt" to FieldValue.serverTimestamp(),
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