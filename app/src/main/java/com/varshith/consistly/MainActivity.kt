package com.varshith.consistly

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.varshith.consistly.data.ConsistlyDatabase
import com.varshith.consistly.data.repositories.StreakRepository
import com.varshith.consistly.navigation.ConsistlyNavGraph
import com.varshith.consistly.services.NotificationService
import com.varshith.consistly.services.ReminderNotificationService
import com.varshith.consistly.services.StreakReminderService
import com.varshith.consistly.ui.theme.ConsistlyTheme
import com.varshith.consistly.viewmodels.StreakViewModel

class MainActivity : ComponentActivity() {
    private lateinit var notificationService: NotificationService
    private lateinit var streakReminderService: StreakReminderService
    private lateinit var reminderNotificationService: ReminderNotificationService
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            notificationService.showWelcomeNotification()
//            streakReminderService.scheduleDaily()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Notification Services
        notificationService = NotificationService(this)
        streakReminderService = StreakReminderService(this)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    notificationService.showWelcomeNotification()
                    streakReminderService.scheduleDaily()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            notificationService.showWelcomeNotification()
            streakReminderService.scheduleDaily()
        }

        // Initialize Database and ViewModel
        val database = ConsistlyDatabase.getDatabase(applicationContext)
        val streakDao = database.streakDao()
        val streakRepository = StreakRepository(streakDao)

        val streakViewModel: StreakViewModel by viewModels {
            StreakViewModel.provideFactory(streakRepository,context = applicationContext)
        }

        setContent {
            ConsistlyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    ConsistlyNavGraph(
                        navController = navController,
                        viewModel = streakViewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Optional: Cancel reminder when app is closed
        // streakReminderService.cancelDailyReminder()
    }
}