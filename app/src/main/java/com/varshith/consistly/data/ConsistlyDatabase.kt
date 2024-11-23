package com.varshith.consistly.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.varshith.consistly.data.datasource.StreakDao
import com.varshith.consistly.data.models.StreakEntity
import com.varshith.consistly.utils.LocalDateConverter
import com.varshith.consistly.utils.LocalDateListConverter

@Database(entities = [StreakEntity::class], version = 2, exportSchema = false)
@TypeConverters(
    LocalDateConverter::class,
    LocalDateListConverter::class
)
abstract class ConsistlyDatabase : RoomDatabase() {
    abstract fun streakDao(): StreakDao

    companion object {
        @Volatile
        private var INSTANCE: ConsistlyDatabase? = null

        fun getDatabase(context: Context): ConsistlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ConsistlyDatabase::class.java,
                    "consistly_database"
                )
                    .fallbackToDestructiveMigration() // Use carefully in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}