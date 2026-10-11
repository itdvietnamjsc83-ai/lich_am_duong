package vn.itdvietnam.lichamduong.reminder

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import vn.itdvietnam.lichamduong.R
import vn.itdvietnam.lichamduong.lunar.CanChi
import vn.itdvietnam.lichamduong.lunar.DayInfo
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Chuông báo thức: phát chuông (theo âm lượng báo thức của máy, kêu cả khi máy để im lặng),
 * rung liên tục và hiện màn hình chuông trên màn hình khoá cho đến khi người dùng bấm
 * Tắt hoặc Báo lại. Tự tắt sau [RING_MINUTES] phút và để lại thông báo "đã lỡ".
 */
class AlarmService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timeout = Runnable { onTimeout() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRinging(intent)
            ACTION_STOP -> stopRinging()
            ACTION_SNOOZE -> snooze()
            else -> if (_ringing.value == null) stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startRinging(intent: Intent) {
        val reminder = ReminderRepository.get(this).find(intent.getLongExtra(EXTRA_ID, -1L))
        val date = intent.getStringExtra(EXTRA_DATE)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()
        if (reminder == null) {
            if (_ringing.value == null) stopSelf()
            return
        }
        // Đang có chuông khác: dừng âm thanh cũ, chuyển sang nhắc mới
        stopSound()
        val info = RingInfo(reminder.id, reminder.title, date, reminder.ruleText(), reminder.snoozeMinutes)
        _ringing.value = info

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(info),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0,
        )
        playSound()
        vibrate()
        handler.removeCallbacks(timeout)
        handler.postDelayed(timeout, RING_MINUTES * 60_000L)
    }

    private fun buildNotification(info: RingInfo): Notification {
        val day = DayInfo.of(info.date)
        val text = "${day.weekday}, ${day.solarText} · ${CanChi.lunarShort(day.lunar)} âm lịch"
        val openAlarm = PendingIntent.getActivity(
            this, REQ_ACTIVITY,
            Intent(this, AlarmActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, ReminderNotifications.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_moon)
            .setContentTitle(info.title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text + "\n" + info.rule))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAlarm)
            .setFullScreenIntent(openAlarm, true)
            .addAction(0, "Báo lại ${info.snoozeMinutes} phút", servicePending(this, ACTION_SNOOZE, REQ_SNOOZE))
            .addAction(0, "Tắt chuông", servicePending(this, ACTION_STOP, REQ_STOP))
            .build()
    }

    private fun playSound() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        for (uri in alarmSoundCandidates(this)) {
            val mp = MediaPlayer()
            val ok = runCatching {
                mp.setAudioAttributes(attrs)
                mp.setDataSource(this, uri)
                mp.isLooping = true
                mp.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)
                mp.prepare()
                mp.start()
            }.isSuccess
            if (ok) {
                player = mp
                return
            }
            mp.release()
        }
    }

    private fun vibrate() {
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
        if (v == null || !v.hasVibrator()) return
        vibrator = v
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
        @Suppress("DEPRECATION")
        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 800, 600), 0), attrs)
    }

    private fun stopSound() {
        player?.let { runCatching { it.stop() }; it.release() }
        player = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun stopRinging() {
        handler.removeCallbacks(timeout)
        stopSound()
        _ringing.value = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun snooze() {
        val info = _ringing.value
        val reminder = info?.let { ReminderRepository.get(this).find(it.reminderId) }
        if (info != null && reminder != null) {
            ReminderScheduler(this).snooze(reminder, info.date, info.snoozeMinutes)
            val at = LocalTime.now().plusMinutes(info.snoozeMinutes.toLong()).format(DateTimeFormatter.ofPattern("HH:mm"))
            Toast.makeText(this, "Sẽ báo lại lúc $at", Toast.LENGTH_LONG).show()
        }
        stopRinging()
    }

    private fun onTimeout() {
        val info = _ringing.value
        val reminder = info?.let { ReminderRepository.get(this).find(it.reminderId) }
        stopRinging()
        if (info != null && reminder != null) ReminderNotifications.showMissed(this, reminder, info.date)
    }

    override fun onDestroy() {
        handler.removeCallbacks(timeout)
        stopSound()
        _ringing.value = null
        super.onDestroy()
    }

    data class RingInfo(val reminderId: Long, val title: String, val date: LocalDate, val rule: String, val snoozeMinutes: Int)

    companion object {
        const val ACTION_START = "vn.itdvietnam.lichamduong.alarm.START"
        const val ACTION_STOP = "vn.itdvietnam.lichamduong.alarm.STOP"
        const val ACTION_SNOOZE = "vn.itdvietnam.lichamduong.alarm.SNOOZE"
        private const val EXTRA_ID = "reminder_id"
        private const val EXTRA_DATE = "occurrence_date"
        const val RING_MINUTES = 5
        private const val NOTIFICATION_ID = 0x7A1A12
        private const val REQ_ACTIVITY = 0x7A0001
        private const val REQ_SNOOZE = 0x7A0002
        private const val REQ_STOP = 0x7A0003

        private val _ringing = MutableStateFlow<RingInfo?>(null)

        /** Chuông đang kêu (null khi không có), để màn hình chuông theo dõi. */
        val ringing: StateFlow<RingInfo?> = _ringing.asStateFlow()

        fun start(context: Context, reminderId: Long, date: LocalDate) {
            val intent = Intent(context, AlarmService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_ID, reminderId)
                .putExtra(EXTRA_DATE, date.toString())
            ContextCompat.startForegroundService(context, intent)
        }

        fun command(context: Context, action: String) {
            context.startService(Intent(context, AlarmService::class.java).setAction(action))
        }

        private fun servicePending(context: Context, action: String, requestCode: Int): PendingIntent =
            PendingIntent.getService(
                context, requestCode,
                Intent(context, AlarmService::class.java).setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        private fun alarmSoundCandidates(context: Context): List<Uri> = listOfNotNull(
            RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM),
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
        ).distinct()

        /** Nghe thử chuông báo thức trong 3 giây. */
        fun preview(context: Context) {
            val uri = alarmSoundCandidates(context).firstOrNull() ?: return
            val ringtone = RingtoneManager.getRingtone(context, uri) ?: return
            ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            runCatching { ringtone.play() }
            Handler(Looper.getMainLooper()).postDelayed({ runCatching { ringtone.stop() } }, 3000)
        }
    }
}
