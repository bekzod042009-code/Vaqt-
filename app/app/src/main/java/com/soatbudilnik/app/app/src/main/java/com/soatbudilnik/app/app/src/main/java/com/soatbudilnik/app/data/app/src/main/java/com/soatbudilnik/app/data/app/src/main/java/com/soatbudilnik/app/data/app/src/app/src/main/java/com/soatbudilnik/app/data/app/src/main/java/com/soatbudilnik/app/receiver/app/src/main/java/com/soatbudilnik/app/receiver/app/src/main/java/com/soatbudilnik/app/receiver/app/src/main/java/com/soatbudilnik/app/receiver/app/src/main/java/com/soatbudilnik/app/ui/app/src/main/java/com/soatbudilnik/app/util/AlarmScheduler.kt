package com.soatbudilnik.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.soatbudilnik.app.data.Alarm
import com.soatbudilnik.app.data.AnnounceInterval
import com.soatbudilnik.app.data.Reminder
import com.soatbudilnik.app.receiver.AlarmReceiver
import com.soatbudilnik.app.receiver.ReminderReceiver
import com.soatbudilnik.app.receiver.TimeAnnouncerReceiver
import java.util.Calendar

object AlarmScheduler {

    private const val REQ_ALARM_BASE = 10_000
    private const val REQ_REMINDER_BASE = 20_000
    private const val REQ_ANNOUNCER = 30_000

    private fun nextTrigger(hour: Int, minute: Int, repeatDays: Set<Int>): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (repeatDays.isEmpty()) {
            if (target.before(now)) target.add(Calendar.DAY_OF_MONTH, 1)
            return target.timeInMillis
        }
        // Takrorlanuvchi: bugundan boshlab 7 kun ichida mos kunni topamiz
        for (i in 0..7) {
            val candidate = target.clone() as Calendar
            candidate.add(Calendar.DAY_OF_MONTH, i)
            val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK) // 1=Yakshanba..7=Shanba
            if (repeatDays.contains(dayOfWeek) && !candidate.before(now)) {
                return candidate.timeInMillis
            }
        }
        return target.timeInMillis
    }

    fun scheduleAlarm(context: Context, alarm: Alarm) {
        if (!alarm.isEnabled) { cancelAlarm(context, alarm.id); return }
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
        }
        val pi = PendingIntent.getBroadcast(
            context, REQ_ALARM_BASE + alarm.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = nextTrigger(alarm.hour, alarm.minute, alarm.repeatDays)
        am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, pi), pi)
    }

    fun cancelAlarm(context: Context, alarmId: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, REQ_ALARM_BASE + alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    fun scheduleReminder(context: Context, reminder: Reminder) {
        if (!reminder.isEnabled) { cancelReminder(context, reminder.id); return }
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
        }
        val pi = PendingIntent.getBroadcast(
            context, REQ_REMINDER_BASE + reminder.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = nextTrigger(reminder.hour, reminder.minute, reminder.repeatDays)
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }

    fun cancelReminder(context: Context, reminderId: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, REQ_REMINDER_BASE + reminderId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    /** Avtomatik vaqtni ovoz bilan aytish uchun keyingi intervalni rejalashtiradi (masalan har 1 soatda). */
    fun scheduleNextAnnouncer(context: Context, interval: AnnounceInterval) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimeAnnouncerReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, REQ_ANNOUNCER, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (interval == AnnounceInterval.OFF) {
            am.cancel(pi)
            return
        }
        val triggerAt = System.currentTimeMillis() + interval.minutes * 60_000L
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }

    fun cancelAnnouncer(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimeAnnouncerReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, REQ_ANNOUNCER, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }
}
