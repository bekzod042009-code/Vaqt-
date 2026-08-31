package com.soatbudilnik.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.soatbudilnik.app.data.AppDatabase
import com.soatbudilnik.app.data.SettingsRepository
import com.soatbudilnik.app.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Telefon qayta yoqilgach barcha budilnik, eslatma va avtomatik ovozli vaqtni tiklaydi. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            db.alarmDao().getAll().first().forEach { AlarmScheduler.scheduleAlarm(context, it) }
            db.reminderDao().getAll().first().forEach { AlarmScheduler.scheduleReminder(context, it) }

            val settings = SettingsRepository(context).settingsFlow.first()
            AlarmScheduler.scheduleNextAnnouncer(context, settings.announceInterval)
        }
    }
}
