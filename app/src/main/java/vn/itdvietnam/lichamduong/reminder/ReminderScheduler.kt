package vn.itdvietnam.lichamduong.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Đặt báo cho lần nhắc kế tiếp. Sau mỗi lần báo, [ReminderReceiver] đặt tiếp lần sau. */
class ReminderScheduler(private val context: Context) {

    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(reminder: Reminder) {
        cancel(reminder.id)
        val next = Recurrence.nextAlarm(reminder, LocalDateTime.now()) ?: return
        val triggerAt = next.second.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pending = pendingIntent(reminder.id, next.first)
        setAlarm(triggerAt, pending, reminder.alarm)
    }

    /** Báo lại sau [minutes] phút cho lần nhắc ngày [date] (dùng khi bấm "Báo lại" trên màn hình chuông). */
    fun snooze(reminder: Reminder, date: LocalDate, minutes: Int) {
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(ACTION_REMIND)
            .putExtra(EXTRA_ID, reminder.id)
            .putExtra(EXTRA_DATE, date.toString())
            .putExtra(EXTRA_SNOOZE, true)
        val pending = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt() + SNOOZE_REQUEST_OFFSET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        setAlarm(triggerAt, pending, reminder.alarm)
    }

    private fun setAlarm(triggerAt: Long, pending: PendingIntent, alarmClock: Boolean) {
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        try {
            if (canExact && alarmClock) {
                val show = PendingIntent.getActivity(
                    context, 0,
                    Intent(context, vn.itdvietnam.lichamduong.MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, show), pending)
            } else if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancel(id: Long) {
        alarmManager.cancel(pendingIntent(id, null))
        val snooze = PendingIntent.getBroadcast(
            context, id.toInt() + SNOOZE_REQUEST_OFFSET,
            Intent(context, ReminderReceiver::class.java).setAction(ACTION_REMIND),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (snooze != null) alarmManager.cancel(snooze)
    }

    private fun pendingIntent(id: Long, date: LocalDate?): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .setAction(ACTION_REMIND)
            .putExtra(EXTRA_ID, id)
        if (date != null) intent.putExtra(EXTRA_DATE, date.toString())
        return PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_REMIND = "vn.itdvietnam.lichamduong.action.REMIND"
        const val EXTRA_ID = "reminder_id"
        const val EXTRA_DATE = "occurrence_date"
        const val EXTRA_SNOOZE = "snooze"
        private const val SNOOZE_REQUEST_OFFSET = 1_000_000
    }
}
