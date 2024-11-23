package com.varshith.consistly.data.datasource

import com.varshith.consistly.data.models.Streak
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocalStreakDataSource {
    private val _streaks = MutableStateFlow<List<Streak>>(emptyList())
    val streaks: StateFlow<List<Streak>> = _streaks.asStateFlow()

    fun insertStreak(streak: Streak) {
        _streaks.update { currentStreaks ->
            currentStreaks + streak
        }
    }

    fun updateStreak(streak: Streak) {
        _streaks.update { currentStreaks ->
            currentStreaks.map {
                if (it.id == streak.id) streak else it
            }
        }
    }

    fun deleteStreak(streakId: String) {
        _streaks.update { currentStreaks ->
            currentStreaks.filterNot { it.id == streakId }
        }
    }

    fun getStreakById(streakId: String): Streak? {
        return _streaks.value.find { it.id == streakId }
    }
}