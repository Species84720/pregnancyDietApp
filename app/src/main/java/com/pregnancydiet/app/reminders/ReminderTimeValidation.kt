package com.pregnancydiet.app.reminders

import java.time.LocalTime

object ReminderTimeValidation {
    fun validate(preferences: com.pregnancydiet.app.model.ReminderPreferences): Result<Unit> = runCatching {
        if (preferences.mealRemindersEnabled) {
            require(preferences.mealReminderTime.isValidTime()) { "Meal reminder time must use HH:MM, for example 12:30." }
        }
        if (preferences.symptomCheckInEnabled) {
            require(preferences.symptomReminderTime.isValidTime()) { "Symptom check-in time must use HH:MM, for example 20:00." }
        }
    }

    private fun String.isValidTime(): Boolean = runCatching { LocalTime.parse(trim()) }.isSuccess
}
