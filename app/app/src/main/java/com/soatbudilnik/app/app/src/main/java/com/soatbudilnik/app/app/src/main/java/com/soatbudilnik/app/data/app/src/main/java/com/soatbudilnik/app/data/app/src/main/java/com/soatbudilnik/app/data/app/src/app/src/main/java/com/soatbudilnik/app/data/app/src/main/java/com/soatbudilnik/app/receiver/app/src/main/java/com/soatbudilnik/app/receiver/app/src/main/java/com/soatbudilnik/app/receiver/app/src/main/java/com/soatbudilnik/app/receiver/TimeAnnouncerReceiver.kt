package com.soatbudilnik.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import com.soatbudilnik.app.data.SettingsRepository
import com.soatbudilnik.app.util.AlarmScheduler
import com.soatbudilnik.app.util.TimeAnnouncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Avtomatik vaqtni ovoz bilan aytish (masalan har 1 soatda "10:00").
 * Uyqu rejimi oralig'ida (masalan 22:30-07:00) bu funksiya ISHLAMAYDI,
 * lekin oddiy budilniklar bunga bog'liq emas.
 */
class TimeAnnouncerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            val repo = SettingsRepository(context)
            val settings = repo.settingsFlow.first()
            val now = Calendar.getInstance()
            val hour = now.get(Calendar.HOUR_OF_DAY)
            val minute = now.get(Calendar.MINUTE)

            val inSleepWindow = repo.isWithinSleepWindow(settings, hour, minute)

            if (!inSleepWindow) {
                if (settings.vibrationEnabled) {
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                TimeAnnouncer(context) { }.announceTime(hour, minute)
            }

            // Har doim keyingi intervalni qayta rejalashtiramiz (uyqu paytida ham,
            // shunda uyqu tugagach funksiya avtomatik davom etadi)
            AlarmScheduler.scheduleNextAnnouncer(context, settings.announceInterval)
        }
    }
}
