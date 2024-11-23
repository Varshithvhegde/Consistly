package com.varshith.consistly.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.varshith.consistly.data.repositories.GoalFrequency
import java.time.LocalDate
import java.time.LocalTime
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