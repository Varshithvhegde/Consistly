package com.varshith.consistly.data.models
import java.time.LocalDate
import java.util.UUID

data class Streak  constructor(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val startDate: LocalDate = LocalDate.now(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isActive: Boolean = true,
    val dailyLogDates: List<LocalDate> = listOf()
)