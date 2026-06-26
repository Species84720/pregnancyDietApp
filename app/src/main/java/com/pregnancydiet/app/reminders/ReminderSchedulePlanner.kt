package com.pregnancydiet.app.reminders

import com.pregnancydiet.app.model.ReminderPreferences
import com.pregnancydiet.app.model.SupplementWithTodayStatus
import java.time.LocalTime
import kotlin.math.absoluteValue

object ReminderSchedulePlanner {
    fun plan(
        preferences: ReminderPreferences,
        supplements: List<SupplementWithTodayStatus>,
    ): ReminderPlan {
        val scheduled = mutableListOf<ReminderScheduleRequest>()
        var skipped = 0

        if (preferences.supplementRemindersEnabled) {
            supplements
                .map { it.supplement }
                .filter { it.active }
                .forEach { supplement ->
                    val time = supplement.timeOfDay.takeIfValidTime()
                    if (time == null) {
                        skipped += 1
                    } else {
                        scheduled += ReminderScheduleRequest(
                            notificationId = ReminderType.Supplement.notificationIdBase + supplement.id.stableNotificationOffset(),
                            type = ReminderType.Supplement,
                            timeOfDay = time,
                            title = ReminderCopy.supplementTitle(supplement),
                            body = ReminderCopy.supplementBody(supplement),
                        )
                    }
                }
        }

        if (preferences.mealRemindersEnabled) {
            val time = preferences.mealReminderTime.takeIfValidTime()
            if (time == null) {
                skipped += 1
            } else {
                scheduled += ReminderScheduleRequest(
                    notificationId = ReminderType.Meal.notificationIdBase,
                    type = ReminderType.Meal,
                    timeOfDay = time,
                    title = ReminderCopy.mealTitle(),
                    body = ReminderCopy.mealBody(),
                )
            }
        }

        if (preferences.symptomCheckInEnabled) {
            val time = preferences.symptomReminderTime.takeIfValidTime()
            if (time == null) {
                skipped += 1
            } else {
                scheduled += ReminderScheduleRequest(
                    notificationId = ReminderType.Symptom.notificationIdBase,
                    type = ReminderType.Symptom,
                    timeOfDay = time,
                    title = ReminderCopy.symptomTitle(),
                    body = ReminderCopy.symptomBody(),
                )
            }
        }

        return ReminderPlan(
            requests = scheduled.distinctBy { it.notificationId },
            skippedCount = skipped,
        )
    }
}

data class ReminderPlan(
    val requests: List<ReminderScheduleRequest>,
    val skippedCount: Int,
)

private fun String.takeIfValidTime(): String? = runCatching { LocalTime.parse(trim()) }
    .getOrNull()
    ?.toString()

private fun String.stableNotificationOffset(): Int = hashCode().absoluteValue % 900
