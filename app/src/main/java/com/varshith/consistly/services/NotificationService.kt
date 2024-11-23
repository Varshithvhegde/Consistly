package com.varshith.consistly.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.varshith.consistly.R

class NotificationService(private val context: Context) {

    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = "Consistly Notifications"
        val descriptionText = "Notifications for Consistly app"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun showWelcomeNotification() {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Replace with your app icon
            .setContentTitle("Welcome to Consistly!")
            .setContentText("Keep track of your streaks and stay consistent!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Show the notification
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "consistly_channel"
        const val NOTIFICATION_ID = 1
    }
}