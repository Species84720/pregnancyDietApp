package com.pregnancydiet.app.model

import java.time.LocalDateTime

data class ReminderPreferences(
    val supplementRemindersEnabled: Boolean = false,
    val mealRemindersEnabled: Boolean = false,
    val symptomCheckInEnabled: Boolean = false,
    val mealReminderTime: String = DEFAULT_MEAL_REMINDER_TIME,
    val symptomReminderTime: String = DEFAULT_SYMPTOM_REMINDER_TIME,
    val updatedAtIso: String = LocalDateTime.now().toString(),
) {
    val anyEnabled: Boolean = supplementRemindersEnabled || mealRemindersEnabled || symptomCheckInEnabled

    companion object {
        const val DEFAULT_MEAL_REMINDER_TIME = "12:30"
        const val DEFAULT_SYMPTOM_REMINDER_TIME = "20:00"
    }
}
