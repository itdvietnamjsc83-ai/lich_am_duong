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
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Nhận báo thức: hiện thông báo rồi đặt lần nhắc kế tiếp. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_REMIND) return
        val repo = ReminderRepository.get(context)
        val reminder = repo.find(intent.getLongExtra(ReminderScheduler.EXTRA_ID, -1L)) ?: return
        val date = intent.getStringExtra(ReminderScheduler.EXTRA_DATE)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now()
        if (reminder.enabled) ReminderNotifications.show(context, reminder, date)
        ReminderScheduler(context).schedule(reminder)
    }
}

/** Đặt lại toàn bộ lịch báo sau khi khởi động máy, đổi giờ hoặc cập nhật ứng dụng. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderRepository.get(context).rescheduleAll()
    }
}

object ReminderNotifications {
    const val CHANNEL_ID = "nhac_lich"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Nhắc lịch", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Thông báo nhắc lịch theo âm lịch và dương lịch"
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
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
