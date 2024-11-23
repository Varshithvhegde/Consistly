package com.varshith.consistly.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.varshith.consistly.R
import com.varshith.consistly.MainActivity
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ReminderNotificationService(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "streak_reminders"
        const val CHANNEL_NAME = "Streak Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for streak reminders"
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

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleStreakReminder(
        streakId: String,
        streakName: String,
        reminderTime: LocalTime
    ) {
        val workManager = WorkManager.getInstance(context)

        // Cancel any existing reminders for this streak
        workManager.cancelAllWorkByTag("streak_reminder_$streakId")

        // Calculate initial delay
        val currentTime = LocalDateTime.now()
        var scheduledTime = LocalDateTime.now().with(reminderTime)

        // If the time has already passed today, schedule for tomorrow
        if (currentTime.isAfter(scheduledTime)) {
            scheduledTime = scheduledTime.plusDays(1)
        }

        val initialDelay = scheduledTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                currentTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Create work request for the reminder
        val reminderRequest = PeriodicWorkRequestBuilder<StreakReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .addTag("streak_reminder_$streakId")
            .setInputData(
                workDataOf(
                    "streak_id" to streakId,
                    "streak_name" to streakName
                )
            )
            .build()

        // Enqueue the work request
        workManager.enqueueUniquePeriodicWork(
            "streak_reminder_$streakId",
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderRequest
        )
    }

    fun cancelStreakReminder(streakId: Long) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("streak_reminder_$streakId")
    }
}
