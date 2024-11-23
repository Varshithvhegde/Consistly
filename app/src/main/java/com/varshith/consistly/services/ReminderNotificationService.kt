package com.varshith.consistly.services

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.varshith.consistly.MainActivity
import com.varshith.consistly.R
import com.varshith.consistly.services.NotificationReceiver
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderNotificationService(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    companion object {
        const val CHANNEL_ID = "streak_reminders"
        const val CHANNEL_NAME = "Streak Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for streak reminders"
        const val NOTIFICATION_ACTION = "com.varshith.consistly.SHOW_NOTIFICATION"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleStreakReminder(
        streakId: String,
        streakName: String,
        reminderTime: LocalTime
    ) {
        println(streakId)
        // Create intent for the broadcast receiver
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NOTIFICATION_ACTION
            putExtra("streak_id", streakId)
            putExtra("streak_name", streakName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            streakId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate the first trigger time
        val currentTime = LocalDateTime.now()
        var scheduledTime = LocalDateTime.now().with(reminderTime)

        if (currentTime.isAfter(scheduledTime)) {
            scheduledTime = scheduledTime.plusDays(1)
        }

        val triggerTime = scheduledTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Schedule the alarm
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelStreakReminder(streakId: String) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val notificationId = streakId.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun showNotification(streakId: String, streakName: String) {
        val notificationId = streakId.hashCode()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("streak_id", streakId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val motivationalMessages = listOf(
            "Time to keep your streak going! 🔥",
            "Don't break the chain! 💪",
            "Your future self will thank you! ⭐",
            "Consistency is key! 🔑",
            "Let's make it happen! 🎯"
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(streakName)
            .setContentText(motivationalMessages.random())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }
    fun rescheduleAllReminders() {
        // Implement this method to reschedule all reminders from your database
        // This should be called after device reboot
    }
}