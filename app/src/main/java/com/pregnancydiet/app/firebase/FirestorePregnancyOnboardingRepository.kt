package com.pregnancydiet.app.firebase

import com.google.firebase.firestore.FieldValue
import com.pregnancydiet.app.data.OnboardingSaveResult
import com.pregnancydiet.app.data.PregnancyOnboardingRepository
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.WeightLog
import com.pregnancydiet.app.onboarding.ValidatedOnboardingInput
import com.pregnancydiet.app.pregnancy.PregnancyProgress
import kotlinx.coroutines.tasks.await

class FirestorePregnancyOnboardingRepository : PregnancyOnboardingRepository {
    override suspend fun saveOnboardingProfile(
        uid: String,
        input: ValidatedOnboardingInput,
        pregnancyProfile: PregnancyProfile,
        progress: PregnancyProgress,
    ): Result<OnboardingSaveResult> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val userRef = firestore.collection(USERS_COLLECTION).document(uid)
        val profileRef = userRef.collection(PREGNANCY_PROFILES_COLLECTION).document()
        val weightLogRef = userRef.collection(WEIGHT_LOGS_COLLECTION).document()
        val profileWithId = pregnancyProfile.copy(id = profileRef.id)
        val weightLog = WeightLog(
            id = weightLogRef.id,
            date = input.dateFoundOut.toString(),
            pregnancyProfileId = profileRef.id,
            pregnancyWeek = progress.pregnancyWeek,
            weightKg = input.currentWeightKg,
        )

        firestore.runBatch { batch ->
            batch.set(profileRef, profileWithId.toFirestoreMap(progress))
            batch.set(weightLogRef, weightLog.toFirestoreMap())
            batch.update(
                userRef,
                mapOf(
                    "onboardingCompleted" to true,
                    "activePregnancyProfileId" to profileRef.id,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            )
        }.await()

        OnboardingSaveResult(profileWithId, weightLog)
    }
}

private const val USERS_COLLECTION = "users"
private const val PREGNANCY_PROFILES_COLLECTION = "pregnancyProfiles"
private const val WEIGHT_LOGS_COLLECTION = "weightLogs"

private fun PregnancyProfile.toFirestoreMap(progress: PregnancyProgress): Map<String, Any?> = mapOf(
    "dateFoundOut" to dateFoundOut,
    "lastMenstrualPeriod" to lastMenstrualPeriod,
    "estimatedDueDate" to estimatedDueDate,
    "calculatedEstimatedDueDate" to progress.estimatedDueDate?.toString(),
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
    "pregnancyWeek" to progress.pregnancyWeek,
    "dayWithinWeek" to progress.dayWithinWeek,
    "trimester" to progress.trimester,
    "datingMethod" to progress.datingMethod.name,
    "createdAt" to FieldValue.serverTimestamp(),
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