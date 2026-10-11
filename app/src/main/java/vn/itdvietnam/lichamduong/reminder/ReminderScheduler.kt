package vn.itdvietnam.lichamduong.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Đặt báo cho lần nhắc kế tiếp. Mỗi nhắc lịch có thể có hai mốc riêng:
 * - Thông báo (hiện thông báo thường) vào giờ đã chọn.
 * - Đổ chuông (như báo thức) vào giờ chuông đã chọn, dùng setAlarmClock để báo đúng giờ nhất.
 * Sau mỗi lần báo, [ReminderReceiver] đặt tiếp lần sau.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(reminder: Reminder) {
        cancel(reminder.id)
        val now = LocalDateTime.now()
        if (!reminder.ringReplacesNotification) {
            Recurrence.nextAlarm(reminder, now)?.let { (date, at) ->
                setAlarm(millis(at), pendingIntent(reminder.id, date, KIND_NOTIFY), alarmClock = false)
            }
        }
        Recurrence.nextRing(reminder, now)?.let { (date, at) ->
            setAlarm(millis(at), pendingIntent(reminder.id, date, KIND_RING), alarmClock = true)
        }
    }

    /** Báo lại sau [minutes] phút cho lần nhắc ngày [date] (khi bấm "Báo lại" trên màn hình chuông). */
    fun snooze(reminder: Reminder, date: LocalDate, minutes: Int) {
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        val intent = baseIntent(reminder.id)
            .putExtra(EXTRA_DATE, date.toString())
            .putExtra(EXTRA_KIND, KIND_RING)
            .putExtra(EXTRA_SNOOZE, true)
        val pending = PendingIntent.getBroadcast(
            context, requestCode(reminder.id, SNOOZE_OFFSET), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        setAlarm(triggerAt, pending, alarmClock = true)
    }

    fun cancel(id: Long) {
        for (offset in listOf(0, RING_OFFSET, SNOOZE_OFFSET)) {
            val pending = PendingIntent.getBroadcast(
                context, requestCode(id, offset), baseIntent(id),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            if (pending != null) {
                alarmManager.cancel(pending)
                pending.cancel()
            }
        }
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

    private fun pendingIntent(id: Long, date: LocalDate, kind: String): PendingIntent {
        val intent = baseIntent(id)
            .putExtra(EXTRA_DATE, date.toString())
            .putExtra(EXTRA_KIND, kind)
        return PendingIntent.getBroadcast(
            context, requestCode(id, if (kind == KIND_RING) RING_OFFSET else 0), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun baseIntent(id: Long): Intent =
        Intent(context, ReminderReceiver::class.java)
            .setAction(ACTION_REMIND)
            .putExtra(EXTRA_ID, id)

    private fun requestCode(id: Long, offset: Int): Int = (id % 1_000_000).toInt() + offset

    private fun millis(at: LocalDateTime): Long = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    companion object {
        const val ACTION_REMIND = "vn.itdvietnam.lichamduong.action.REMIND"
        const val EXTRA_ID = "reminder_id"
        const val EXTRA_DATE = "occurrence_date"
        const val EXTRA_KIND = "kind"
        const val EXTRA_SNOOZE = "snooze"
        const val KIND_NOTIFY = "notify"
        const val KIND_RING = "ring"
        private const val RING_OFFSET = 2_000_000
        private const val SNOOZE_OFFSET = 1_000_000
    }
}
