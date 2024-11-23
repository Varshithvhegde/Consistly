package com.varshith.consistly.data.repositories

import com.varshith.consistly.data.datasource.StreakDao
import com.varshith.consistly.data.models.StreakEntity
import com.varshith.consistly.data.models.getAchievements
import com.varshith.consistly.data.models.getMilestones
import com.varshith.consistly.data.models.getMonthlyStats
import com.varshith.consistly.data.models.getSkipDates
import com.varshith.consistly.data.models.getWeeklyStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class StreakRepository(private val streakDao: StreakDao) {
    val streaks: Flow<List<StreakEntity>> = streakDao.getAllStreaks()

    suspend fun addStreak(streak: StreakEntity): String {
        // Generate a UUID for the streak if it doesn't have one
        val streakToAdd = if (streak.id.isBlank()) {
            streak.copy(id = java.util.UUID.randomUUID().toString())
        } else {
            streak
        }

        // Insert and return the ID
        streakDao.insertStreak(streakToAdd)
        return streakToAdd.id
    }

    suspend fun updateStreak(streak: StreakEntity) {
        streakDao.updateStreak(streak)
    }

    suspend fun deleteStreak(streakId: String) {
        streakDao.deleteStreak(streakId)
    }

    suspend fun getStreak(streakId: String): StreakEntity? {
        return streakDao.getStreakById(streakId).first()
    }

    suspend fun logStreakDay(streakId: String): Boolean {
        val today = LocalDate.now()
        val streak = getStreak(streakId) ?: return false

        // Check if streak can be logged today
//        if (!canLogStreak(streak, today)) {
//            println("Can also Log today")
//            return false
//        }
//        println("What is wrong")

        // Update the streak with new log
        val updatedLogDates = streak.dailyLogDates + today
        val consecutiveDays = calculateConsecutiveDays(updatedLogDates)

        val updatedStreak = streak.copy(
            dailyLogDates = updatedLogDates,
            currentStreak = consecutiveDays,
            longestStreak = maxOf(streak.longestStreak, consecutiveDays),
            lastCompletedDate = today,
            totalCompletedDays = streak.totalCompletedDays + 1,
            averageCompletionRate = calculateCompletionRate(updatedLogDates, streak.startDate),
            weeklyStatsJson = updateWeeklyStats(streak, today),
            monthlyStatsJson = updateMonthlyStats(streak, today),
            modifiedAt = today
        )
        println(updatedStreak.toString())

        // Check for milestone achievements
        val achievements = checkAndUpdateAchievements(updatedStreak)
        if (achievements.isNotEmpty()) {
            println("Inside IF")
            updateStreak(updatedStreak.copy(
                achievementsJson = updateAchievements(updatedStreak.achievementsJson, achievements)
            ))
        } else {
            println("Updating Streak")
            updateStreak(updatedStreak)
        }

        return true
    }

    suspend fun breakStreak(streakId: String) {
        val streak = getStreak(streakId) ?: return

        val updatedStreak = streak.copy(
            currentStreak = 0,
            isActive = false,
            modifiedAt = LocalDate.now()
        )
        updateStreak(updatedStreak)
    }

    fun getStreaksByCategory(category: String): Flow<List<StreakEntity>> {
        return streaks.map { it.filter { streak -> streak.category == category } }
    }

    fun getActiveStreaks(): Flow<List<StreakEntity>> {
        return streaks.map { it.filter { streak -> streak.isActive } }
    }

    fun getStreaksNeedingAttention(): Flow<List<StreakEntity>> {
        return streaks.map { allStreaks ->
            allStreaks.filter { streak ->
                streak.isActive &&
                        !streak.dailyLogDates.contains(LocalDate.now()) &&
                        shouldCompleteToday(streak) &&
                        !isGracePeriodActive(streak)
            }
        }
    }

    private fun calculateMonthlyCompletion(streak: StreakEntity): Map<LocalDate, Int> {
        val monthlyMap = mutableMapOf<LocalDate, Int>()
        val startDate = streak.dailyLogDates.minOrNull() ?: return monthlyMap
        var currentMonth = startDate.withDayOfMonth(1)

        while (currentMonth <= LocalDate.now()) {
            val completedDays = streak.dailyLogDates.count {
                it >= currentMonth && it < currentMonth.plusMonths(1)
            }
            monthlyMap[currentMonth] = completedDays
            currentMonth = currentMonth.plusMonths(1)
        }
        return monthlyMap
    }

    private fun calculateWeeklyCompletion(streak: StreakEntity): Map<LocalDate, Int> {
        val weeklyMap = mutableMapOf<LocalDate, Int>()
        val startDate = streak.dailyLogDates.minOrNull() ?: return weeklyMap
        var currentWeek = startDate.with(DayOfWeek.MONDAY)

        while (currentWeek <= LocalDate.now()) {
            val completedDays = streak.dailyLogDates.count {
                it >= currentWeek && it < currentWeek.plusWeeks(1)
            }
            weeklyMap[currentWeek] = completedDays
            currentWeek = currentWeek.plusWeeks(1)
        }
        return weeklyMap
    }

    fun getStreakStatistics(streakId: String): Flow<StreakStatistics> {
        return streakDao.getStreakById(streakId).map { streak ->
            streak?.let {
                StreakStatistics(
                    totalDaysCompleted = it.totalCompletedDays,
                    currentStreak = it.currentStreak,
                    longestStreak = it.longestStreak,
                    weeklyCompletion = calculateWeeklyCompletion(it),
                    monthlyCompletion = calculateMonthlyCompletion(it),
                    averageCompletionRate = it.averageCompletionRate,
                    lastCompletedDate = it.lastCompletedDate,
                    milestones = it.getMilestones(),
                    achievedMilestones = it.getAchievements()
                )
            } ?: StreakStatistics()
        }
    }

    private fun canLogStreak(streak: StreakEntity, date: LocalDate): Boolean {
        println(when {
            streak.dailyLogDates.contains(date) -> false
            !streak.isActive -> false
//            isGracePeriodExpired(streak) -> false
//            hasExceededAllowedSkips(streak) -> false
            else -> true
        })
        return when {
            streak.dailyLogDates.contains(date) -> false
            !streak.isActive -> false
            isGracePeriodExpired(streak) -> false
            hasExceededAllowedSkips(streak) -> false
            else -> true
        }
    }

    private fun isGracePeriodActive(streak: StreakEntity): Boolean {
        val lastCompleted = streak.lastCompletedDate ?: return true
        val hoursSinceLastCompletion = ChronoUnit.HOURS.between(
            lastCompleted.atTime(LocalTime.MAX),
            LocalDate.now().atTime(LocalTime.now())
        )
        return hoursSinceLastCompletion <= streak.gracePeriodinHours
    }

    private fun isGracePeriodExpired(streak: StreakEntity): Boolean {
        return !isGracePeriodActive(streak)
    }

    private fun hasExceededAllowedSkips(streak: StreakEntity): Boolean {
        val currentMonth = LocalDate.now().withDayOfMonth(1)
        val skipsThisMonth = streak.getSkipDates()
            .count { it.month == currentMonth.month && it.year == currentMonth.year }
        return skipsThisMonth >= streak.allowedSkipsPerMonth
    }

    private fun calculateConsecutiveDays(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0

        val sortedDates = dates.sorted()
        var currentStreak = 1
        var maxStreak = 1

        for (i in 1 until sortedDates.size) {
            val daysBetween = ChronoUnit.DAYS.between(sortedDates[i-1], sortedDates[i])
            if (daysBetween == 1L) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }

        return maxStreak
    }

    private fun calculateCompletionRate(dates: List<LocalDate>, startDate: LocalDate): Float {
        val totalDays = ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1
        return (dates.size.toFloat() / totalDays.toFloat()) * 100
    }

    private fun updateWeeklyStats(streak: StreakEntity, today: LocalDate): String {
        val weeklyStats = streak.getWeeklyStats().toMutableMap()
        val weekNumber = today.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
        weeklyStats[weekNumber] = (weeklyStats[weekNumber] ?: 0) + 1
        return com.google.gson.Gson().toJson(weeklyStats)
    }

    private fun updateMonthlyStats(streak: StreakEntity, today: LocalDate): String {
        val monthlyStats = streak.getMonthlyStats().toMutableMap()
        val monthNumber = today.monthValue
        monthlyStats[monthNumber] = (monthlyStats[monthNumber] ?: 0) + 1
        return com.google.gson.Gson().toJson(monthlyStats)
    }

    private fun checkAndUpdateAchievements(streak: StreakEntity): List<String> {
        val achievements = mutableListOf<String>()
        val milestones = streak.getMilestones()

        if (streak.currentStreak in milestones) {
            achievements.add("Reached ${streak.currentStreak} day streak!")
        }

        if (streak.totalCompletedDays in listOf(7, 30, 100, 365)) {
            achievements.add("Completed ${streak.totalCompletedDays} total days!")
        }

        return achievements
    }

    private fun updateAchievements(currentJson: String, newAchievements: List<String>): String {
        val current = try {
            com.google.gson.Gson().fromJson(currentJson, Array<String>::class.java).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
        current.addAll(newAchievements)
        return com.google.gson.Gson().toJson(current)
    }

    private fun shouldCompleteToday(streak: StreakEntity): Boolean {
        return when (streak.goalFrequency) {
            GoalFrequency.DAILY -> true
            GoalFrequency.WEEKLY -> getCompletedDaysInCurrentWeek(streak) < streak.targetDays
            GoalFrequency.MONTHLY -> getCompletedDaysInCurrentMonth(streak) < streak.targetDays
        }
    }

    private fun getCompletedDaysInCurrentWeek(streak: StreakEntity): Int {
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        return streak.dailyLogDates.count { it >= startOfWeek && it <= today }
    }

    private fun getCompletedDaysInCurrentMonth(streak: StreakEntity): Int {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        return streak.dailyLogDates.count { it >= startOfMonth && it <= today }
    }
}

data class StreakStatistics(
    val totalDaysCompleted: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val weeklyCompletion: Map<LocalDate, Int> = emptyMap(),
    val monthlyCompletion: Map<LocalDate, Int> = emptyMap(),
    val averageCompletionRate: Float = 0f,
    val lastCompletedDate: LocalDate? = null,
    val milestones: List<Int> = listOf(7, 30, 60, 90, 180, 365),
    val achievedMilestones: List<String> = emptyList()
)
// Supporting data classes and enums
enum class GoalFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}
