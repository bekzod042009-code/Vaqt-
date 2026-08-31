package com.soatbudilnik.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.soatbudilnik.app.data.AppDatabase
import com.soatbudilnik.app.ui.AlarmRingActivity
import com.soatbudilnik.app.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Budilnik vaqti keldi: signal turidan qat'i nazar (ovoz/vibratsiya/musiqa)
 * to'liq ekranli AlarmRingActivity'ni ochadi, u yerda haqiqiy signal ijro etiladi.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        if (alarmId == -1) return

        val ringIntent = Intent(context, AlarmRingActivity::class.java).apply {
            putExtra("alarm_id", alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(ringIntent)

        // Takrorlanuvchi budilnik bo'lsa, keyingi kunga qayta rejalashtiramiz
        CoroutineScope(Dispatchers.IO).launch {
            val dao = AppDatabase.getInstance(context).alarmDao()
            val alarm = dao.getById(alarmId) ?: return@launch
            if (alarm.repeatDays.isNotEmpty()) {
                AlarmScheduler.scheduleAlarm(context, alarm)
            }
        }
    }
}
