package vn.itdvietnam.lichamduong.reminder

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vn.itdvietnam.lichamduong.lunar.CanChi
import vn.itdvietnam.lichamduong.lunar.DayInfo
import vn.itdvietnam.lichamduong.ui.theme.LichTheme
import vn.itdvietnam.lichamduong.ui.theme.NumberFont
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Màn hình chuông: hiện cả trên màn hình khoá, bật sáng màn hình, nút Tắt và Báo lại cỡ lớn. */
class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        setContent {
            val info by AlarmService.ringing.collectAsStateWithLifecycle()
            LaunchedEffect(info) { if (info == null) finish() }
            LichTheme(dark = true, textScale = 1f) {
                info?.let { ring ->
                    AlarmScreen(
                        ring = ring,
                        onSnooze = { send(AlarmService.ACTION_SNOOZE) },
                        onStop = { send(AlarmService.ACTION_STOP) },
                    )
                }
            }
        }
    }

    private fun send(action: String) {
        AlarmService.command(this, action)
        finish()
    }
}

@Composable
private fun AlarmScreen(ring: AlarmService.RingInfo, onSnooze: () -> Unit, onStop: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val day = remember(ring.date) { DayInfo.of(ring.date) }
    val now = remember { LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = cs.primary, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(12.dp))
        Text(now, fontFamily = NumberFont, fontWeight = FontWeight.Bold, fontSize = 80.sp, lineHeight = 84.sp, color = cs.onBackground)
        Spacer(Modifier.height(16.dp))
        Text(
            ring.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = cs.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${day.weekday}, ${day.solarText}",
            style = MaterialTheme.typography.titleMedium,
            color = cs.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            CanChi.lunarDateText(day.lunar) + " âm lịch",
            style = MaterialTheme.typography.titleMedium,
            color = cs.primary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(ring.rule, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
            ) { Text("Báo lại sau ${AlarmService.SNOOZE_MINUTES} phút", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
            Button(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth().height(76.dp),
                shape = RoundedCornerShape(24.dp),
            ) { Text("Tắt chuông", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(8.dp))
    }
}
