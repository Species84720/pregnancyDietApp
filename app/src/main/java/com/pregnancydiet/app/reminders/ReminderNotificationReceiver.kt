package com.pregnancydiet.app.reminders

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pregnancydiet.app.MainActivity

class ReminderNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SHOW_REMINDER) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Gentle reminder" }
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty().ifBlank {
            "Open Pregnancy Diet Tracker when you have a moment."
        }
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val contentIntent = android.app.PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, ReminderCopy.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val ACTION_SHOW_REMINDER = "com.pregnancydiet.app.reminders.SHOW_REMINDER"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_TYPE = "type"
    }
}
