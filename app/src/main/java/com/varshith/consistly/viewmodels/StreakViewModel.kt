package com.varshith.consistly.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.varshith.consistly.data.models.StreakEntity
import com.varshith.consistly.data.repositories.GoalFrequency
import com.varshith.consistly.data.repositories.StreakRepository
import com.varshith.consistly.services.ReminderNotificationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class StreakViewModel(
    private val streakRepository: StreakRepository,
    private val context: Context
) : ViewModel() {

    // State holders for filtered views
    private val _filterCategory = MutableStateFlow<String?>(null)
    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST)
    private val notificationService = ReminderNotificationService(context)
    // Convert Flow to StateFlow for Compose
    val streaks = combine(
        streakRepository.streaks,
        _filterCategory,
        _sortOrder
    ) { streaks, category, sortOrder ->
        var filteredStreaks = if (category != null) {
            streaks.filter { it.category == category }
        } else {
            streaks
        }

        when (sortOrder) {
            SortOrder.NEWEST -> filteredStreaks.sortedByDescending { it.createdAt }
            SortOrder.OLDEST -> filteredStreaks.sortedBy { it.createdAt }
            SortOrder.HIGHEST_STREAK -> filteredStreaks.sortedByDescending { it.currentStreak }
            SortOrder.ALPHABETICAL -> filteredStreaks.sortedBy { it.name }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active streaks for quick access
    val activeStreaks = streaks.map { streaks ->
        streaks.filter { it.isActive }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun createStreak(
        name: String,
        description: String? = null,
        category: String? = null,
        goalFrequency: GoalFrequency = GoalFrequency.DAILY,
        targetDays: Int = 1,
        reminderEnabled: Boolean = false,
        reminderTimeString: String? = null,
        color: String = "#FF4081",
        icon: String? = null,
        priority: Int = 0
    ) {
        println(reminderTimeString)
        viewModelScope.launch {
            try {
                val newStreak = StreakEntity(
                    name = name,
                    description = description,
                    category = category,
                    goalFrequency = goalFrequency,
                    targetDays = targetDays,
                    reminderEnabled = reminderEnabled,
                    reminderTimeString = reminderTimeString,
                    color = color,
                    icon = icon,
                    priority = priority,
                    startDate = LocalDate.now(),
                    createdAt = LocalDate.now(),
                    modifiedAt = LocalDate.now()
                )

                val streakId = streakRepository.addStreak(newStreak)
                println("Created streak with ID: $streakId")

                // Schedule reminder if enabled
                if (reminderEnabled && reminderTimeString != null) {
                    try {
                        val reminderTime = LocalTime.parse(reminderTimeString, DateTimeFormatter.ofPattern("HH:mm"))
                        notificationService.scheduleStreakReminder(
                            streakId = streakId,
                            streakName = name,
                            reminderTime = reminderTime
                        )
                        println("Reminder set successfully for streak: $streakId")
                    } catch (e: Exception) {
                        println("Failed to set reminder: ${e.message}")
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                println("Failed to create streak: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    fun updateStreak(
        streakId: String,
        name: String? = null,
        description: String? = null,
        category: String? = null,
        goalFrequency: GoalFrequency? = null,
        targetDays: Int? = null,
        reminderEnabled: Boolean? = null,
        reminderTimeString: String? = null,
        color: String? = null,
        icon: String? = null,
        priority: Int? = null
    ) {
        viewModelScope.launch {
            streakRepository.getStreak(streakId)?.let { currentStreak ->
                val updatedStreak = currentStreak.copy(
                    name = name ?: currentStreak.name,
                    description = description ?: currentStreak.description,
                    category = category ?: currentStreak.category,
                    goalFrequency = goalFrequency ?: currentStreak.goalFrequency,
                    targetDays = targetDays ?: currentStreak.targetDays,
                    reminderEnabled = reminderEnabled ?: currentStreak.reminderEnabled,
                    reminderTimeString = reminderTimeString ?: currentStreak.reminderTimeString,
                    color = color ?: currentStreak.color,
                    icon = icon ?: currentStreak.icon,
                    priority = priority ?: currentStreak.priority,
                    modifiedAt = LocalDate.now()
                )
                streakRepository.updateStreak(updatedStreak)

                // Update reminder
                if (reminderEnabled != null || reminderTimeString != null) {
                    val isReminderEnabled = reminderEnabled ?: updatedStreak.reminderEnabled
                    val timeString = reminderTimeString ?: updatedStreak.reminderTimeString

                    if (isReminderEnabled && timeString != null) {
                        try {
                            val reminderTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
                            notificationService.scheduleStreakReminder(
                                streakId = streakId,
                                streakName = updatedStreak.name,
                                reminderTime = reminderTime
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        notificationService.cancelStreakReminder(streakId)
                    }
                }
            }
        }
    }

    fun logStreakDay(streakId: String) {
        println("Log Streak clicked")
        viewModelScope.launch {
            streakRepository.logStreakDay(streakId)
        }
    }

    fun breakStreak(streakId: String) {
        viewModelScope.launch {
            streakRepository.breakStreak(streakId)
        }
    }

    fun getStreakById(streakId: String): StreakEntity? {
        return streaks.value.find { it.id == streakId }
    }

    fun deleteStreak(streakId: String) {
        viewModelScope.launch {
            // Cancel any existing reminders before deleting the streak
            notificationService.cancelStreakReminder(streakId)
            streakRepository.deleteStreak(streakId)
        }
    }

    fun setFilterCategory(category: String?) {
        _filterCategory.value = category
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    fun toggleStreakActive(streakId: String) {
        viewModelScope.launch {
            streakRepository.getStreak(streakId)?.let { streak ->
                val updatedStreak = streak.copy(
                    isActive = !streak.isActive,
                    modifiedAt = LocalDate.now()
                )
                streakRepository.updateStreak(updatedStreak)
            }
        }
    }

    fun getReminderTime(timeString: String?): LocalTime? {
        return timeString?.let {
            try {
                LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) {
                null
            }
        }
    }

    // Enums for sorting options
    enum class SortOrder {
        NEWEST,
        OLDEST,
        HIGHEST_STREAK,
        ALPHABETICAL
    }

    // Factory for ViewModel creation with dependencies
    companion object {
        fun provideFactory(
            streakRepository: StreakRepository,
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StreakViewModel(streakRepository, context) as T
            }
        }
    }
}