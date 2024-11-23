package com.varshith.consistly.services

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.varshith.consistly.MainActivity
import com.varshith.consistly.R
import com.varshith.consistly.services.NotificationService.Companion
import java.util.Calendar

class StreakReminderService(private val context: Context) {

    private val notificationManagerCompat = NotificationManagerCompat.from(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Streak Reminder Notifications"
        val descriptionText = "Daily reminders to log your streaks"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun showDailyNotificationMessage(){
        val builder = NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Replace with your app icon
            .setContentTitle("Streak Reminder Notifications!")
            .setContentText("Daily reminders to log your streaks!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Show the notification
        notificationManagerCompat.notify(NotificationService.NOTIFICATION_ID, builder.build())
    }

    fun scheduleDaily() {
        val intent = Intent(context, StreakReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set time for daily reminder (e.g., 7:00 PM)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 10) // 7 PM
            set(Calendar.MINUTE, 58)
            set(Calendar.SECOND, 0)
        }

        // If the time has passed today, schedule for next day
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Set repeating alarm
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyReminder() {
        val intent = Intent(context, StreakReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        notificationManagerCompat.cancel(NOTIFICATION_ID)
    }

    class StreakReminderReceiver : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val notificationIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingNotificationIntent = PendingIntent.getActivity(
                context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Streak Check-In")
                .setContentText("Don't break your streak! Log your progress for today.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingNotificationIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "streak_reminder_channel"
        const val NOTIFICATION_ID = 2
        const val REMINDER_REQUEST_CODE = 100
    }
}