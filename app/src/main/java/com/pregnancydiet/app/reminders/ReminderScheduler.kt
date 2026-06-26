package com.pregnancydiet.app.reminders

import com.pregnancydiet.app.model.ReminderPreferences
import com.pregnancydiet.app.model.SupplementWithTodayStatus

interface ReminderScheduler {
    fun schedule(
        preferences: ReminderPreferences,
        supplements: List<SupplementWithTodayStatus>,
    ): ReminderScheduleResult

    fun cancelAll()

    fun createNotificationChannel()
}
