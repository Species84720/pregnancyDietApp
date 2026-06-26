package com.pregnancydiet.app.reminders

data class ReminderScheduleRequest(
    val notificationId: Int,
    val type: ReminderType,
    val timeOfDay: String,
    val title: String,
    val body: String,
)

data class ReminderScheduleResult(
    val scheduledCount: Int,
    val skippedCount: Int,
) {
    val userMessage: String = when {
        scheduledCount > 0 && skippedCount > 0 -> "Scheduled $scheduledCount reminders. $skippedCount reminders were skipped because a time was missing or invalid."
        scheduledCount > 0 -> "Scheduled $scheduledCount gentle reminders."
        skippedCount > 0 -> "No reminders were scheduled because times were missing or invalid."
        else -> "Reminders are turned off."
    }
}
