package com.pregnancydiet.app.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pregnancydiet.app.model.ReminderPreferences
import com.pregnancydiet.app.model.SupplementWithTodayStatus
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AndroidReminderScheduler(
    private val context: Context,
) : ReminderScheduler {
    private val alarmManager: AlarmManager? = context.getSystemService(AlarmManager::class.java)

    override fun schedule(
        preferences: ReminderPreferences,
        supplements: List<SupplementWithTodayStatus>,
    ): ReminderScheduleResult {
        createNotificationChannel()
        cancelAll()
        val plan = ReminderSchedulePlanner.plan(preferences, supplements)
        plan.requests.forEach(::scheduleRequest)
        return ReminderScheduleResult(
            scheduledCount = plan.requests.size,
            skippedCount = plan.skippedCount,
        )
    }

    override fun cancelAll() {
        listOf(ReminderType.Supplement, ReminderType.Meal, ReminderType.Symptom).forEach { type ->
            val range = type.notificationIdBase until type.notificationIdBase + REQUEST_CODE_BUCKET_SIZE
            range.forEach { requestCode -> alarmManager?.cancel(pendingIntent(requestCode, null)) }
        }
    }

    override fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            ReminderCopy.CHANNEL_ID,
            ReminderCopy.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = ReminderCopy.CHANNEL_DESCRIPTION
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleRequest(request: ReminderScheduleRequest) {
        val triggerMillis = request.timeOfDay.toNextTriggerMillis()
        alarmManager?.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent(request.notificationId, request),
        )
    }

    private fun pendingIntent(
        requestCode: Int,
        request: ReminderScheduleRequest?,
    ): PendingIntent {
        val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
            action = ReminderNotificationReceiver.ACTION_SHOW_REMINDER
            putExtra(ReminderNotificationReceiver.EXTRA_NOTIFICATION_ID, requestCode)
            if (request != null) {
                putExtra(ReminderNotificationReceiver.EXTRA_TITLE, request.title)
                putExtra(ReminderNotificationReceiver.EXTRA_BODY, request.body)
                putExtra(ReminderNotificationReceiver.EXTRA_TYPE, request.type.name)
            }
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun String.toNextTriggerMillis(): Long {
        val time = LocalTime.parse(this)
        val now = LocalDateTime.now()
        val scheduled = now.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0)
        val next = if (scheduled.isAfter(now)) scheduled else scheduled.plusDays(1)
        return next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private companion object {
        const val REQUEST_CODE_BUCKET_SIZE = 1_000
    }
}
