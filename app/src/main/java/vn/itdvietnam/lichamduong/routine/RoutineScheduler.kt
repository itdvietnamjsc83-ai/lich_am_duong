package vn.itdvietnam.lichamduong.routine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import vn.itdvietnam.lichamduong.MainActivity
import vn.itdvietnam.lichamduong.reminder.ReminderReceiver
import vn.itdvietnam.lichamduong.reminder.ReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

/**
 * Đặt báo cho lần kế tiếp của từng việc lặp (mỗi việc một mốc; báo xong đặt tiếp mốc sau).
 * Việc "đổ chuông" dùng setAlarmClock (như ứng dụng Đồng hồ) để báo đúng giờ nhất.
 */
class RoutineScheduler(private val context: Context) {

    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(routine: Routine, after: LocalDateTime = LocalDateTime.now()) {
        cancel(routine.id)
        val now = LocalDateTime.now()
        val (date, minute) = RoutineSchedule.next(routine, if (after.isAfter(now)) after else now) ?: return
        val at = date.atTime(minute / 60, minute % 60)
        set(millis(at), pending(routine.id, date, minute, OFFSET, snooze = false), routine.ring)
    }

    fun snooze(routine: Routine, date: LocalDate, minute: Int, minutes: Int) {
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        set(triggerAt, pending(routine.id, date, minute, SNOOZE_OFFSET, snooze = true), alarmClock = true)
    }

    fun cancel(id: Long) {
        for (offset in listOf(OFFSET, SNOOZE_OFFSET)) {
            val p = PendingIntent.getBroadcast(
                context, code(id, offset), base(id),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            if (p != null) {
                alarmManager.cancel(p)
                p.cancel()
            }
        }
    }

    private fun pending(id: Long, date: LocalDate, minute: Int, offset: Int, snooze: Boolean): PendingIntent {
        val intent = base(id)
            .putExtra(ReminderScheduler.EXTRA_DATE, date.toString())
            .putExtra(EXTRA_MINUTE, minute)
            .putExtra(ReminderScheduler.EXTRA_SNOOZE, snooze)
        return PendingIntent.getBroadcast(
            context, code(id, offset), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun base(id: Long): Intent = Intent(context, ReminderReceiver::class.java)
        .setAction(ReminderScheduler.ACTION_REMIND)
        .putExtra(ReminderScheduler.EXTRA_ID, id)
        .putExtra(ReminderScheduler.EXTRA_KIND, KIND_ROUTINE)

    private fun set(triggerAt: Long, pending: PendingIntent, alarmClock: Boolean) {
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        try {
            if (canExact && alarmClock) {
                val show = PendingIntent.getActivity(
                    context, 0, Intent(context, MainActivity::class.java),
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

    private fun code(id: Long, offset: Int) = (id % 1_000_000).toInt() + offset
    private fun millis(at: LocalDateTime) = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    companion object {
        const val KIND_ROUTINE = "routine"
        const val EXTRA_MINUTE = "minute"
        private const val OFFSET = 3_000_000
        private const val SNOOZE_OFFSET = 4_000_000

        /**
         * Thêm các giờ của việc lặp vào ứng dụng Đồng hồ của máy (báo thức hệ thống, lặp theo thứ).
         * Trả về số báo thức đã gửi sang Đồng hồ; 0 nếu máy không có ứng dụng hỗ trợ.
         */
        fun addToSystemClock(context: Context, routine: Routine): Int {
            val days = ArrayList(routine.days.sorted().map { if (it == 7) Calendar.SUNDAY else it + 1 })
            var count = 0
            for (m in routine.times) {
                val intent = Intent(AlarmClock.ACTION_SET_ALARM)
                    .putExtra(AlarmClock.EXTRA_HOUR, m / 60)
                    .putExtra(AlarmClock.EXTRA_MINUTES, m % 60)
                    .putExtra(AlarmClock.EXTRA_MESSAGE, routine.displayTitle.take(60))
                    .putExtra(AlarmClock.EXTRA_DAYS, days)
                    .putExtra(AlarmClock.EXTRA_VIBRATE, true)
                    .putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(intent)
                    count++
                } catch (e: ActivityNotFoundException) {
                    return count
                } catch (e: SecurityException) {
                    return count
                }
            }
            return count
        }
    }
}
