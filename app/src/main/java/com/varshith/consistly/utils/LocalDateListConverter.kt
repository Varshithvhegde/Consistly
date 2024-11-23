package com.varshith.consistly.utils

import androidx.room.TypeConverter
import java.time.LocalDate

object LocalDateListConverter {
    private const val SEPARATOR = "|"

    @TypeConverter
    fun fromLocalDateList(dates: List<LocalDate>?): String? {
        return dates?.map { it.toString() }?.joinToString(separator = SEPARATOR)
    }

    @TypeConverter
    fun toLocalDateList(datesString: String?): List<LocalDate> {
        return datesString
            ?.takeIf { it.isNotBlank() }
            ?.split(SEPARATOR)
            ?.map { LocalDate.parse(it) }
            ?: emptyList()
    }

    // Helper method to check if a date exists in the list
    fun isDateInList(dateListString: String?, dateToCheck: LocalDate): Boolean {
        return dateListString
            ?.split(SEPARATOR)
            ?.map { LocalDate.parse(it) }
            ?.contains(dateToCheck)
            ?: false
    }
}