package com.pregnancydiet.app.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.pregnancydiet.app.data.ReminderPreferencesRepository
import com.pregnancydiet.app.model.ReminderPreferences
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

class FirestoreReminderPreferencesRepository : ReminderPreferencesRepository {
    override suspend fun loadReminderPreferences(uid: String): Result<ReminderPreferences> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(REMINDER_PREFERENCES_COLLECTION)
            .document(DEFAULT_REMINDER_DOCUMENT)
            .get()
            .await()
            .takeIf { it.exists() }
            ?.toReminderPreferences()
            ?: ReminderPreferences()
    }

    override suspend fun saveReminderPreferences(
        uid: String,
        preferences: ReminderPreferences,
    ): Result<ReminderPreferences> = runCatching {
        val firestore = FirebaseConfiguration.firestoreOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val savedPreferences = preferences.copy(updatedAtIso = LocalDateTime.now().toString())
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(REMINDER_PREFERENCES_COLLECTION)
            .document(DEFAULT_REMINDER_DOCUMENT)
            .set(savedPreferences.toFirestoreMap())
            .await()
        savedPreferences
    }
}

private const val USERS_COLLECTION = "users"
private const val REMINDER_PREFERENCES_COLLECTION = "reminderPreferences"
private const val DEFAULT_REMINDER_DOCUMENT = "default"

private fun ReminderPreferences.toFirestoreMap(): Map<String, Any?> = mapOf(
    "supplementRemindersEnabled" to supplementRemindersEnabled,
    "mealRemindersEnabled" to mealRemindersEnabled,
    "symptomCheckInEnabled" to symptomCheckInEnabled,
    "mealReminderTime" to mealReminderTime,
    "symptomReminderTime" to symptomReminderTime,
    "updatedAtIso" to updatedAtIso,
    "updatedAt" to FieldValue.serverTimestamp(),
)

private fun DocumentSnapshot.toReminderPreferences(): ReminderPreferences = ReminderPreferences(
    supplementRemindersEnabled = getBoolean("supplementRemindersEnabled") ?: false,
    mealRemindersEnabled = getBoolean("mealRemindersEnabled") ?: false,
    symptomCheckInEnabled = getBoolean("symptomCheckInEnabled") ?: false,
    mealReminderTime = getString("mealReminderTime") ?: ReminderPreferences.DEFAULT_MEAL_REMINDER_TIME,
    symptomReminderTime = getString("symptomReminderTime") ?: ReminderPreferences.DEFAULT_SYMPTOM_REMINDER_TIME,
    updatedAtIso = getString("updatedAtIso").orEmpty(),
)
