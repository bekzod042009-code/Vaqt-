package com.soatbudilnik.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

enum class AnnounceInterval(val minutes: Int, val label: String) {
    OFF(0, "O'chirilgan"),
    MIN_1(1, "Har 1 daqiqada"),
    MIN_5(5, "Har 5 daqiqada"),
    MIN_15(15, "Har 15 daqiqada"),
    MIN_30(30, "Har 30 daqiqada"),
    HOUR_1(60, "Har 1 soatda")
}

data class AppSettings(
    val is24HourFormat: Boolean = true,
    val announceInterval: AnnounceInterval = AnnounceInterval.OFF,
    val vibrationEnabled: Boolean = true,
    val vibrationDurationMs: Long = 5000L,
    val sleepModeEnabled: Boolean = false,
    val sleepStartHour: Int = 22,
    val sleepStartMinute: Int = 30,
    val sleepEndHour: Int = 7,
    val sleepEndMinute: Int = 0,
    val darkMode: Boolean = false
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val FORMAT_24H = booleanPreferencesKey("format_24h")
        val ANNOUNCE_INTERVAL = intPreferencesKey("announce_interval_minutes")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val VIBRATION_DURATION = longPreferencesKey("vibration_duration_ms")
        val SLEEP_ENABLED = booleanPreferencesKey("sleep_enabled")
        val SLEEP_START_H = intPreferencesKey("sleep_start_h")
        val SLEEP_START_M = intPreferencesKey("sleep_start_m")
        val SLEEP_END_H = intPreferencesKey("sleep_end_h")
        val SLEEP_END_M = intPreferencesKey("sleep_end_m")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            is24HourFormat = prefs[Keys.FORMAT_24H] ?: true,
            announceInterval = AnnounceInterval.values()
                .firstOrNull { it.minutes == (prefs[Keys.ANNOUNCE_INTERVAL] ?: 0) }
                ?: AnnounceInterval.OFF,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            vibrationDurationMs = prefs[Keys.VIBRATION_DURATION] ?: 5000L,
            sleepModeEnabled = prefs[Keys.SLEEP_ENABLED] ?: false,
            sleepStartHour = prefs[Keys.SLEEP_START_H] ?: 22,
            sleepStartMinute = prefs[Keys.SLEEP_START_M] ?: 30,
            sleepEndHour = prefs[Keys.SLEEP_END_H] ?: 7,
            sleepEndMinute = prefs[Keys.SLEEP_END_M] ?: 0,
            darkMode = prefs[Keys.DARK_MODE] ?: false
        )
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { prefs ->
            val existing = AppSettings(
                is24HourFormat = prefs[Keys.FORMAT_24H] ?: true,
                announceInterval = AnnounceInterval.values()
                    .firstOrNull { it.minutes == (prefs[Keys.ANNOUNCE_INTERVAL] ?: 0) }
                    ?: AnnounceInterval.OFF,
                vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
                vibrationDurationMs = prefs[Keys.VIBRATION_DURATION] ?: 5000L,
                sleepModeEnabled = prefs[Keys.SLEEP_ENABLED] ?: false,
                sleepStartHour = prefs[Keys.SLEEP_START_H] ?: 22,
                sleepStartMinute = prefs[Keys.SLEEP_START_M] ?: 30,
                sleepEndHour = prefs[Keys.SLEEP_END_H] ?: 7,
                sleepEndMinute = prefs[Keys.SLEEP_END_M] ?: 0,
                darkMode = prefs[Keys.DARK_MODE] ?: false
            )
            val updated = transform(existing)
            prefs[Keys.FORMAT_24H] = updated.is24HourFormat
            prefs[Keys.ANNOUNCE_INTERVAL] = updated.announceInterval.minutes
            prefs[Keys.VIBRATION_ENABLED] = updated.vibrationEnabled
            prefs[Keys.VIBRATION_DURATION] = updated.vibrationDurationMs
            prefs[Keys.SLEEP_ENABLED] = updated.sleepModeEnabled
            prefs[Keys.SLEEP_START_H] = updated.sleepStartHour
            prefs[Keys.SLEEP_START_M] = updated.sleepStartMinute
            prefs[Keys.SLEEP_END_H] = updated.sleepEndHour
            prefs[Keys.SLEEP_END_M] = updated.sleepEndMinute
            prefs[Keys.DARK_MODE] = updated.darkMode
        }
    }

    /** 22:30–07:00 kabi oraliqda hozir uyqu rejimi faolmi tekshiradi (kecha oralig'ini ham hisobga oladi). */
    fun isWithinSleepWindow(settings: AppSettings, nowHour: Int, nowMinute: Int): Boolean {
        if (!settings.sleepModeEnabled) return false
        val nowMinutes = nowHour * 60 + nowMinute
        val startMinutes = settings.sleepStartHour * 60 + settings.sleepStartMinute
        val endMinutes = settings.sleepEndHour * 60 + settings.sleepEndMinute
        return if (startMinutes <= endMinutes) {
            nowMinutes in startMinutes until endMinutes
        } else {
            // Masalan 22:30 -> 07:00, kechani kesib o'tadi
            nowMinutes >= startMinutes || nowMinutes < endMinutes
        }
    }
}
