package com.varshith.consistly.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.varshith.consistly.MainActivity
import com.varshith.consistly.R

class StreakReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val streakId = inputData.getString("streak_id") ?: return Result.failure()
        val streakName = inputData.getString("streak_name") ?: return Result.failure()

        showNotification(streakId, streakName)
        return Result.success()
    }

    private fun showNotification(streakId: String, streakName: String) {
        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("streak_id", streakId)
        }

        // Generate a consistent notification ID from the string ID
        val notificationId = streakId.hashCode()

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
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

        val notification = NotificationCompat.Builder(applicationContext, NotificationService.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(streakName)
            .setContentText(motivationalMessages.random())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}