package com.varshith.consistly.utils

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateConverter {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, formatter) }
    }

    @TypeConverter
    fun fromListOfLocalDates(dates: List<LocalDate>?): List<String>? {
        return dates?.map { it.format(formatter) }
    }

    @TypeConverter
    fun toListOfLocalDates(dateStrings: List<String>?): List<LocalDate>? {
        return dateStrings?.map { LocalDate.parse(it, formatter) }
    }
}