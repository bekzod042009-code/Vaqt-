package com.soatbudilnik.app.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.soatbudilnik.app.MainActivity
import com.soatbudilnik.app.R
import com.soatbudilnik.app.data.AppDatabase
import com.soatbudilnik.app.data.SettingsRepository
import com.soatbudilnik.app.util.AlarmScheduler
import com.soatbudilnik.app.util.TimeAnnouncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

const val CHANNEL_ID_REMINDERS = "reminders_channel"

/**
 * Eslatma vaqti keldi.
 * MUHIM QOIDA: ovozli yordamchi faqat vaqtni aytadi ("10:30"),
 * eslatma matnini ("Dars qilish") hech qachon o'qib bermaydi -
 * matn faqat bildirishnomada ko'rsatiladi.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", -1)
        if (reminderId == -1) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val reminder = db.reminderDao().getById(reminderId) ?: return@launch
            val settings = SettingsRepository(context).settingsFlow.first()

            val nowCal = java.util.Calendar.getInstance()
            val inSleep = SettingsRepository(context).isWithinSleepWindow(
                settings, nowCal.get(java.util.Calendar.HOUR_OF_DAY), nowCal.get(java.util.Calendar.MINUTE)
            )
            // Eslatma uyqu rejimidan qat'i nazar bildirishnoma va vibratsiya beradi -
            // spesifikatsiyaga ko'ra uyqu rejimi faqat AVTOMATIK vaqt aytish funksiyasiga ta'sir qiladi.

            if (settings.vibrationEnabled) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            }

            TimeAnnouncer(context) { }.announceTime(reminder.hour, reminder.minute)

            showNotification(context, reminder.hour, reminder.minute, reminder.text)

            if (reminder.repeatDays.isNotEmpty()) {
                AlarmScheduler.scheduleReminder(context, reminder)
            }
        }
    }

    private fun showNotification(context: Context, hour: Int, minute: Int, text: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_REMINDERS, "Eslatmalar", NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(channel)
        }
        val openIntent = Intent(context, MainActivity::class.java)
        val pi = android.app.PendingIntent.getActivity(
            context, 0, openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val timeStr = String.format("%02d:%02d", hour, minute)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(timeStr)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        nm.notify(1000 + hour * 100 + minute, notification)
    }
}
