package com.pregnancydiet.app.data

import com.pregnancydiet.app.model.ReminderPreferences

interface ReminderPreferencesRepository {
    suspend fun loadReminderPreferences(uid: String): Result<ReminderPreferences>

    suspend fun saveReminderPreferences(
        uid: String,
        preferences: ReminderPreferences,
    ): Result<ReminderPreferences>
}
