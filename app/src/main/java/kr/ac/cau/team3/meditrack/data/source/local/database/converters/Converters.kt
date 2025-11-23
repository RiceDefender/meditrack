package kr.ac.cau.team3.meditrack.data.source.local.database.converters

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import kr.ac.cau.team3.meditrack.data.source.local.entities.Frequency
import kr.ac.cau.team3.meditrack.data.source.local.entities.IntakeStatus
import kr.ac.cau.team3.meditrack.data.source.local.entities.TimeOfDay
import kr.ac.cau.team3.meditrack.data.source.local.entities.Weekday
import java.sql.Timestamp
import java.time.LocalTime

class Converters {
    // --- TimeOfDay Enum Converter ---
    @TypeConverter
    fun fromTimeOfDay(value: TimeOfDay): String {
        return value.name
    }
    @TypeConverter
    fun toTimeOfDay(value: String): TimeOfDay {
        return TimeOfDay.valueOf(value)
    }

    // --- LocalTime Converter (Saved as String "14:30") ---
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.toString()
    }
    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }

    // --- Timestamp Converter ---
    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Long? {
        return value?.time
    }
    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it) }
    }

    // --- IntakeStatus Enum ---
    @TypeConverter
    fun fromIntakeStatus(value: IntakeStatus): String {
        return value.name
    }
    @TypeConverter
    fun toIntakeStatus(value: String): IntakeStatus {
        return IntakeStatus.valueOf(value)
    }

    // --- Frequency Enum ---
    @TypeConverter
    fun fromFrequency(value: Frequency): String = value.name
    @TypeConverter
    fun toFrequency(value: String): Frequency = Frequency.valueOf(value)


    // --- Weekday Enum ---
    @TypeConverter
    fun fromWeekday(value: Weekday): String = value.name
    @TypeConverter
    fun toWeekday(value: String): Weekday = Weekday.valueOf(value)
    @TypeConverter
    fun fromWeekdayList(value: List<Weekday>?): String? {
        if (value == null) return null
        val type = object : TypeToken<List<Weekday>>() {}.type
        return Gson().toJson(value, type)
    }

    @TypeConverter
    fun toWeekdayList(value: String?): List<Weekday>? {
        if (value == null) return null
        val type = object : TypeToken<List<Weekday>>() {}.type
        return Gson().fromJson(value, type)
    }
}