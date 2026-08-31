package com.soatbudilnik.app.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromDaySet(days: Set<Int>): String = days.joinToString(",")

    @TypeConverter
    fun toDaySet(data: String): Set<Int> =
        if (data.isBlank()) emptySet() else data.split(",").map { it.toInt() }.toSet()

    @TypeConverter
    fun fromSoundType(type: AlarmSoundType): String = type.name

    @TypeConverter
    fun toSoundType(data: String): AlarmSoundType = AlarmSoundType.valueOf(data)
}
