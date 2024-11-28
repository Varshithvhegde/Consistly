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
import java.time.LocalDate
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
        reminderTime: LocalTime,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        // Validate input dates
        require(!startDate.isAfter(endDate)) { "Start date must be before or equal to end date" }
        println("Start Date : $startDate")
        println("End Date :  $endDate")
        // Create base intent for the broadcast receiver
        val baseIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NOTIFICATION_ACTION
            putExtra("streak_id", streakId)
            putExtra("streak_name", streakName)
        }

        // Calculate the first reminder datetime
        val currentDateTime = LocalDateTime.now()
        var currentReminderDateTime = LocalDateTime.of(
            maxOf(currentDateTime.toLocalDate(), startDate),
            reminderTime
        )

        // If today's reminder time has passed, start from the next day
        if (currentDateTime.isAfter(currentReminderDateTime)) {
            currentReminderDateTime = currentReminderDateTime.plusDays(1)
        }

        // Schedule reminders for each day in the range
        var scheduledDate = currentReminderDateTime.toLocalDate()
        while (!scheduledDate.isAfter(endDate)) {
            println("Scheduling reminder for: $scheduledDate")
            val triggerDateTime = LocalDateTime.of(scheduledDate, reminderTime)
            val triggerMillis = triggerDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            // Create a unique ID for each day's reminder
            val uniqueId = "${streakId}_${scheduledDate}".hashCode()

            // Create a pending intent with the unique ID
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueId,
                baseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule a one-time exact alarm for this specific date
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )

            scheduledDate = scheduledDate.plusDays(1)
        }
    }

    fun cancelStreakReminder(
        streakId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        // Cancel all reminders in the date range
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val uniqueId = "${streakId}_${currentDate}".hashCode()
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            currentDate = currentDate.plusDays(1)
        }
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