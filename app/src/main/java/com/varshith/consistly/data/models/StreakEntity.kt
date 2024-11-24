package com.varshith.consistly.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.varshith.consistly.data.repositories.GoalFrequency
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.WeekFields
import java.util.UUID

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val startDate: LocalDate = LocalDate.now(),
    val dailyLogDates: List<LocalDate> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isActive: Boolean = true,
    val targetDays: Int = 1,
    val goalFrequency: GoalFrequency = GoalFrequency.DAILY,

    // Customization and Display
    val color: String = "#FF4081",
    val icon: String? = null,
    val category: String? = null,
    val priority: Int = 0,

    // Reminders and Notifications
    val reminderTimeString: String? = null, // Store time as HH:mm string
    val reminderEnabled: Boolean = true,
    val customReminderDays: String = "", // Store as comma-separated string

    // Progress Tracking
    val targetEndDate: LocalDate? = null,
    val minimumDaysPerWeek: Int? = null,
    val skipDates: String = "", // Store as comma-separated date strings

    // Statistics and Metrics
    val totalCompletedDays: Int = 0,
    val lastCompletedDate: LocalDate? = null,
    val averageCompletionRate: Float = 0f,
    val weeklyStatsJson: String = "{}", // Store as JSON string
    val monthlyStatsJson: String = "{}", // Store as JSON string

    // Motivation and Notes
    val motivationalQuotesJson: String = "[]", // Store as JSON array string
    val notesJson: String = "[]", // Store as JSON array string
    val milestonesJson: String = "[7,30,60,90,180,365]", // Store as JSON array string

    // Social Features
    val isPublic: Boolean = false,
    val sharedWithJson: String = "[]", // Store as JSON array string
    val tagsJson: String = "[]", // Store as JSON array string

    // Accountability
    val requiresProof: Boolean = false,
    val proofType: ProofType = ProofType.NONE,

    // Flexibility
    val allowedSkipsPerMonth: Int = 0,
    val gracePeriodinHours: Int = 0,

    // Gamification
    val currentPoints: Int = 0,
    val level: Int = 1,
    val achievementsJson: String = "[]", // Store as JSON array string

    // Timestamps
    val createdAt: LocalDate = LocalDate.now(),
    val modifiedAt: LocalDate = LocalDate.now()
)

enum class ProofType {
    NONE,
    PHOTO,
    CHECKBOX,
    TEXT_NOTE,
    LOCATION,
    TIME_LOGGED
}

// Type converters for Room
object StreakTypeConverters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromLocalDateList(dates: List<LocalDate>): String {
        return dates.joinToString(",") { it.toString() }
    }

    @TypeConverter
    fun toLocalDateList(value: String): List<LocalDate> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromGoalFrequency(frequency: GoalFrequency): String {
        return frequency.name
    }

    @TypeConverter
    fun toGoalFrequency(value: String): GoalFrequency {
        return GoalFrequency.valueOf(value)
    }

    @TypeConverter
    fun fromProofType(type: ProofType): String {
        return type.name
    }

    @TypeConverter
    fun toProofType(value: String): ProofType {
        return ProofType.valueOf(value)
    }
}

// Extension properties to handle complex types
fun StreakEntity.getReminderTime(): LocalTime? {
    return reminderTimeString?.let { LocalTime.parse(it) }
}

fun StreakEntity.getCustomReminderDays(): List<Int> {
    return if (customReminderDays.isEmpty()) emptyList()
    else customReminderDays.split(",").map { it.toInt() }
}

fun StreakEntity.getSkipDates(): List<LocalDate> {
    return if (skipDates.isEmpty()) emptyList()
    else skipDates.split(",").map { LocalDate.parse(it) }
}

fun StreakEntity.getWeeklyStats(): Map<Int, Int> {
    return try {
        com.google.gson.Gson().fromJson(weeklyStatsJson, Map::class.java) as Map<Int, Int>
    } catch (e: Exception) {
        emptyMap()
    }
}

fun StreakEntity.getMonthlyStats(): Map<Int, Int> {
    return try {
        com.google.gson.Gson().fromJson(monthlyStatsJson, Map::class.java) as Map<Int, Int>
    } catch (e: Exception) {
        emptyMap()
    }
}

fun StreakEntity.getMotivationalQuotes(): List<String> {
    return try {
        com.google.gson.Gson().fromJson(motivationalQuotesJson, List::class.java) as List<String>
    } catch (e: Exception) {
        emptyList()
    }
}

fun StreakEntity.getNotes(): List<String> {
    return try {
        com.google.gson.Gson().fromJson(notesJson, List::class.java) as List<String>
    } catch (e: Exception) {
        emptyList()
    }
}

fun StreakEntity.getMilestones(): List<Int> {
    return try {
        com.google.gson.Gson().fromJson(milestonesJson, List::class.java) as List<Int>
    } catch (e: Exception) {
        listOf(7, 30, 60, 90, 180, 365)
    }
}

fun StreakEntity.getSharedWith(): List<String> {
    return try {
        com.google.gson.Gson().fromJson(sharedWithJson, List::class.java) as List<String>
    } catch (e: Exception) {
        emptyList()
    }
}

fun StreakEntity.getTags(): List<String> {
    return try {
        com.google.gson.Gson().fromJson(tagsJson, List::class.java) as List<String>
    } catch (e: Exception) {
        emptyList()
    }
}

fun StreakEntity.getAchievements(): List<String> {
    return try {
        com.google.gson.Gson().fromJson(achievementsJson, List::class.java) as List<String>
    } catch (e: Exception) {
        emptyList()
    }
}

fun StreakEntity.calculateStreaks(currentDate: LocalDate = LocalDate.now()): StreakEntity {
    if (dailyLogDates.isEmpty()) {
        return this.copy(currentStreak = 0, longestStreak = 0)
    }

    val sortedDates = dailyLogDates.sorted()
    var currentStreakCount = 0
    var maxStreakCount = 0
    var lastDate: LocalDate? = null

    // Calculate based on goal frequency
    when (goalFrequency) {
        GoalFrequency.DAILY -> {
            // For daily goals, any missed day breaks the streak
            for (date in sortedDates) {
                if (lastDate == null || date == lastDate.plusDays(1)) {
                    currentStreakCount++
                } else {
                    // Break in streak detected
                    currentStreakCount = 1
                }
                maxStreakCount = maxOf(maxStreakCount, currentStreakCount)
                lastDate = date
            }

            // Check if streak is still active
            if (lastDate != null && lastDate != currentDate && lastDate != currentDate.minusDays(1)) {
                currentStreakCount = 0
            }
        }

        GoalFrequency.WEEKLY -> {
            // For weekly goals, check if minimum days per week requirement is met
            val weeklyLogs = dailyLogDates.groupBy { it.get(WeekFields.ISO.weekOfWeekBasedYear()) }

            currentStreakCount = 0
            var consecutiveWeeks = 0

            weeklyLogs.entries.sortedBy { it.key }.forEach { (week, dates) ->
                val daysCompleted = dates.size
                if (daysCompleted >= (minimumDaysPerWeek ?: targetDays)) {
                    consecutiveWeeks++
                    currentStreakCount = consecutiveWeeks
                } else {
                    consecutiveWeeks = 0
                }
                maxStreakCount = maxOf(maxStreakCount, currentStreakCount)
            }

            // Check if current week's progress breaks the streak
            val currentWeek = currentDate.get(WeekFields.ISO.weekOfWeekBasedYear())
            val currentWeekLogs = weeklyLogs[currentWeek]?.size ?: 0
            val daysLeftInWeek = 7 - currentDate.dayOfWeek.value + 1

            if (currentWeekLogs + daysLeftInWeek < (minimumDaysPerWeek ?: targetDays)) {
                currentStreakCount = 0
            }
        }

        GoalFrequency.MONTHLY -> TODO()
    }

    // Handle grace period if enabled
    if (gracePeriodinHours > 0 && currentStreakCount == 0) {
        val lastLogDate = sortedDates.lastOrNull()
        if (lastLogDate != null) {
            val graceDeadline = currentDate.atStartOfDay().plusHours(gracePeriodinHours.toLong())
            if (currentDate.atStartOfDay().isBefore(graceDeadline)) {
                // Still within grace period, maintain previous streak
                currentStreakCount = calculatePreviousStreak(sortedDates)
            }
        }
    }

    return this.copy(
        currentStreak = currentStreakCount,
        longestStreak = maxOf(maxStreakCount, longestStreak),
        totalCompletedDays = dailyLogDates.size,
        lastCompletedDate = sortedDates.lastOrNull(),
        averageCompletionRate = calculateCompletionRate(sortedDates, currentDate)
    )
}

private fun StreakEntity.calculatePreviousStreak(sortedDates: List<LocalDate>): Int {
    var count = 0
    var lastDate: LocalDate? = null

    for (date in sortedDates) {
        if (lastDate == null || date == lastDate.plusDays(1)) {
            count++
        } else {
            count = 1
        }
        lastDate = date
    }
    return count
}

private fun StreakEntity.calculateCompletionRate(
    sortedDates: List<LocalDate>,
    currentDate: LocalDate
): Float {
    if (sortedDates.isEmpty()) return 0f

    val startingDate = maxOf(startDate, sortedDates.first())
    val totalDays = startingDate.until(currentDate).days + 1
    return (sortedDates.size.toFloat() / totalDays) * 100
}

// Helper function to log a new day
fun StreakEntity.logDay(date: LocalDate = LocalDate.now()): StreakEntity {
    // Skip if already logged
    if (date in dailyLogDates) return this

    // Check if date is in skip dates
    if (date in getSkipDates()) return this

    val updatedLogs = dailyLogDates + date
    return this.copy(
        dailyLogDates = updatedLogs,
        modifiedAt = LocalDate.now()
    ).calculateStreaks(date)
}