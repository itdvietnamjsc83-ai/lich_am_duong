package vn.itdvietnam.lichamduong.reminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import vn.itdvietnam.lichamduong.MainActivity
import vn.itdvietnam.lichamduong.R
import vn.itdvietnam.lichamduong.lunar.CanChi
import vn.itdvietnam.lichamduong.lunar.DayInfo
import vn.itdvietnam.lichamduong.routine.Routine
import vn.itdvietnam.lichamduong.routine.RoutineRepository
import vn.itdvietnam.lichamduong.routine.RoutineSchedule
import vn.itdvietnam.lichamduong.routine.RoutineScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/** Nhận báo thức: hiện thông báo rồi đặt lần nhắc kế tiếp. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_REMIND) return
        if (intent.getStringExtra(ReminderScheduler.EXTRA_KIND) == RoutineScheduler.KIND_ROUTINE) {
            onRoutine(context, intent)
            return
        }
        val repo = ReminderRepository.get(context)
        val reminder = repo.find(intent.getLongExtra(ReminderScheduler.EXTRA_ID, -1L)) ?: return
        val date = intent.getStringExtra(ReminderScheduler.EXTRA_DATE)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now()
        val isSnooze = intent.getBooleanExtra(ReminderScheduler.EXTRA_SNOOZE, false)
        val isRing = isSnooze || intent.getStringExtra(ReminderScheduler.EXTRA_KIND) == ReminderScheduler.KIND_RING
        if (reminder.enabled) {
            if (isRing && reminder.alarm) {
                // Đổ chuông như báo thức; nếu máy không cho chạy chuông thì vẫn hiện thông báo
                val started = runCatching { AlarmService.start(context, reminder.id, date) }.isSuccess
                if (!started) ReminderNotifications.show(context, reminder, date)
            } else {
                ReminderNotifications.show(context, reminder, date)
            }
        }
        // Lần báo lại không đặt lịch lần sau (lịch lần sau đã được đặt khi báo lần đầu)
        if (!isSnooze) ReminderScheduler(context).schedule(reminder)
    }
}

/** Báo cho việc lặp hằng ngày, rồi đặt lần kế tiếp. */
private fun onRoutine(context: Context, intent: Intent) {
    val routine = RoutineRepository.get(context).find(intent.getLongExtra(ReminderScheduler.EXTRA_ID, -1L)) ?: return
    val date = intent.getStringExtra(ReminderScheduler.EXTRA_DATE)
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()
    val minute = intent.getIntExtra(RoutineScheduler.EXTRA_MINUTE, LocalTime.now().let { it.hour * 60 + it.minute })
    val isSnooze = intent.getBooleanExtra(ReminderScheduler.EXTRA_SNOOZE, false)
    if (routine.enabled) {
        if (routine.ring) {
            val started = runCatching { AlarmService.startRoutine(context, routine.id, date, minute) }.isSuccess
            if (!started) ReminderNotifications.showRoutine(context, routine, minute)
        } else {
            ReminderNotifications.showRoutine(context, routine, minute)
        }
    }
    if (!isSnooze) RoutineScheduler(context).schedule(routine, after = date.atTime(minute / 60, minute % 60).plusMinutes(1))
}

/** Đặt lại toàn bộ lịch báo sau khi khởi động máy, đổi giờ hoặc cập nhật ứng dụng. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderRepository.get(context).rescheduleAll()
        RoutineRepository.get(context).rescheduleAll()
    }
}

object ReminderNotifications {
    const val CHANNEL_ID = "nhac_lich"
    const val ALARM_CHANNEL_ID = "chuong_bao_thuc"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Nhắc lịch", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Thông báo nhắc lịch theo âm lịch và dương lịch"
            // Kênh chuông báo thức: âm thanh do AlarmService phát (để kêu cả khi máy im lặng), kênh không tự phát tiếng
            val alarm = NotificationChannel(ALARM_CHANNEL_ID, "Chuông báo thức", NotificationManager.IMPORTANCE_HIGH)
            alarm.description = "Nhắc lịch đổ chuông và rung như báo thức"
            alarm.setSound(null, null)
            alarm.enableVibration(false)
            alarm.lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            context.getSystemService(NotificationManager::class.java).createNotificationChannels(listOf(channel, alarm))
        }
    }

    /** Thông báo cho việc lặp hằng ngày. */
    @SuppressLint("MissingPermission")
    fun showRoutine(context: Context, routine: Routine, minute: Int) = showSimple(
        context, ROUTINE_NOTIFY_BASE + (routine.id % 100_000).toInt(),
        RoutineSchedule.format(minute) + " · " + routine.displayTitle,
        routine.note.ifBlank { routine.ruleText() },
    )

    /** Thông báo đơn giản, mở ứng dụng khi chạm. */
    @SuppressLint("MissingPermission")
    fun showSimple(context: Context, id: Int, title: String, text: String) {
        if (!canNotify(context)) return
        val open = PendingIntent.getActivity(
            context, id,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_moon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        NotificationManagerCompat.from(context).notify(id, n)
    }

    private const val ROUTINE_NOTIFY_BASE = 500_000

    /** Chuông đã kêu hết thời gian mà không ai tắt: để lại thông báo "đã lỡ". */
    @SuppressLint("MissingPermission")
    fun showMissed(context: Context, reminder: Reminder, date: LocalDate) {
        if (!canNotify(context)) return
        val info = DayInfo.of(date)
        val open = PendingIntent.getActivity(
            context, reminder.id.toInt(),
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_moon)
            .setContentTitle("Đã lỡ chuông: " + reminder.title)
            .setContentText("${info.weekday}, ${info.solarText} (${CanChi.lunarShort(info.lunar)} âm lịch)")
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        NotificationManagerCompat.from(context).notify(reminder.id.toInt(), notification)
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun show(context: Context, reminder: Reminder, date: LocalDate) {
        if (!canNotify(context)) return
        val info = DayInfo.of(date)
        val days = ChronoUnit.DAYS.between(LocalDate.now(), date)
        val whenText = when {
            days <= 0L -> "Hôm nay"
            days == 1L -> "Ngày mai"
            else -> "Còn $days ngày"
        }
        val text = "$whenText · ${info.weekday}, ${info.solarText} (${CanChi.lunarShort(info.lunar)} âm lịch)"
        val open = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_moon)
            .setContentTitle(reminder.title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text + "\n" + reminder.ruleText()))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        NotificationManagerCompat.from(context).notify(reminder.id.toInt(), notification)
    }
}
