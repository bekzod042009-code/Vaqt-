package com.soatbudilnik.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Budilnik signal turi.
 * VOICE      -> faqat vaqtni ovoz bilan aytadi (masalan "07:30")
 * VIBRATION  -> faqat 5 soniyalik vibratsiya
 * MUSIC      -> tanlangan musiqa ijro etiladi
 */
enum class AlarmSoundType {
    VOICE, VIBRATION, MUSIC
}

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: Set<Int> = emptySet(), // 1=Yakshanba ... 7=Shanba, bo'sh bo'lsa "bir marta"
    val soundType: AlarmSoundType = AlarmSoundType.VOICE,
    val musicUri: String? = null // faqat soundType == MUSIC bo'lganda ishlatiladi
)
