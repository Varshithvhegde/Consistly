package com.varshith.consistly.data.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.varshith.consistly.data.models.StreakEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks")
    fun getAllStreaks(): Flow<List<StreakEntity>>

    @Query("SELECT * FROM streaks WHERE id = :streakId")
    fun getStreakById(streakId: String): Flow<StreakEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: StreakEntity)

    @Update
    suspend fun updateStreak(streak: StreakEntity)

    @Query("DELETE FROM streaks WHERE id = :streakId")
    suspend fun deleteStreak(streakId: String)

    @Query("SELECT CASE WHEN (SELECT dailyLogDates FROM streaks WHERE id = :streakId) IS NULL OR NOT instr((SELECT dailyLogDates FROM streaks WHERE id = :streakId), :logDate) > 0 THEN 1 ELSE 0 END")
    suspend fun canLogStreak(streakId: String, logDate: LocalDate): Boolean
}