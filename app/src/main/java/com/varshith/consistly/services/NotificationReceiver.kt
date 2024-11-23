package com.varshith.consistly.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Received broadcast: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Reschedule all notifications after device reboot
                val notificationService = ReminderNotificationService(context)
                notificationService.rescheduleAllReminders()
            }
            "com.varshith.consistly.SHOW_NOTIFICATION" -> {
                val streakId = intent.getStringExtra("streak_id") ?: return
                val streakName = intent.getStringExtra("streak_name") ?: return

                val notificationService = ReminderNotificationService(context)
                notificationService.showNotification(streakId, streakName)
            }
        }
    }
}