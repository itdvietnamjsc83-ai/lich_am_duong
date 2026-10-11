package vn.itdvietnam.lichamduong.ui.screens

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import vn.itdvietnam.lichamduong.BuildConfig
import vn.itdvietnam.lichamduong.R
import vn.itdvietnam.lichamduong.settings.TextSize
import vn.itdvietnam.lichamduong.settings.ThemeMode
import vn.itdvietnam.lichamduong.ui.components.ChoiceChips
import vn.itdvietnam.lichamduong.ui.components.SectionLabel
import vn.itdvietnam.lichamduong.ui.components.SegmentedChoice

/** Thông tin nhà phát triển. */
private object Developer {
    const val COMPANY = "Công ty cổ phần Công nghệ ITD Việt Nam"
    const val PHONE = "0988095867"
    const val EMAIL = "vanthu.itdvn@gmail.com"
    const val WEBSITE = "itdvietnam.com.vn"
    const val ADDRESS = "Số 1 Ngõ 30 đường Lý Sơn, Hà Nội"
}

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    textSize: TextSize,
    notificationsAllowed: Boolean,
    onThemeMode: (ThemeMode) -> Unit,
    onTextSize: (TextSize) -> Unit,
    onRequestNotifications: () -> Unit,
    reminderCount: Int,
    onExport: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val uri = LocalUriHandler.current
    fun open(link: String) {
        runCatching { uri.openUri(link) }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Giao diện")
            SegmentedChoice(
                options = ThemeMode.entries.map { it.label },
                selectedIndex = themeMode.ordinal,
                onSelect = { onThemeMode(ThemeMode.entries[it]) },
            )
            Text(
                "Giao diện tối dùng nền đen tuyền, giúp tiết kiệm pin trên màn hình AMOLED.",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionLabel("Cỡ chữ")
            ChoiceChips(
                options = TextSize.entries.toList(),
                selected = textSize,
                label = { it.label },
                onSelect = onTextSize,
            )
        }

        Surface(shape = RoundedCornerShape(18.dp), color = cs.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Sao lưu & chuyển máy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Xuất $reminderCount nhắc lịch ra một file để cất giữ (Google Drive, Zalo, máy tính…) hoặc chuyển sang điện thoại khác, " +
                        "kể cả iPhone. Trên máy mới, mở ứng dụng và chọn “Nhập từ file”.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onExport,
                        enabled = reminderCount > 0,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("Xuất file sao lưu", fontWeight = FontWeight.Bold) }
                    OutlinedButton(
                        onClick = onImport,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("Nhập từ file", fontWeight = FontWeight.Bold) }
                }
                Text(
                    "Nhập sẽ thêm vào danh sách hiện có; nhắc lịch trùng sẽ được bỏ qua.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                )
            }
        }

        AlarmPermissionCard()

        if (!notificationsAllowed) {
            Surface(shape = RoundedCornerShape(18.dp), color = cs.primaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Chưa bật thông báo", fontWeight = FontWeight.Bold, color = cs.onPrimaryContainer)
                    Text(
                        "Cần cho phép thông báo để ứng dụng báo ngày giỗ, rằm, mùng 1 và các nhắc lịch khác.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onPrimaryContainer,
                    )
                    Button(onClick = onRequestNotifications) { Text("Cho phép thông báo") }
                }
            }
        }

        Surface(shape = RoundedCornerShape(18.dp), color = cs.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("NHÀ PHÁT TRIỂN", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant)
                        Text("Lịch Âm Dương · Phiên bản ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                    // Nền trắng cố định để logo (chữ D màu đen) luôn rõ, kể cả ở giao diện tối
                    Box(
                        Modifier
                            .size(width = 112.dp, height = 48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.itd_logo),
                            contentDescription = "Logo ITD Vietnam",
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
                Text(Developer.COMPANY, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ContactRow(Icons.Outlined.Call, "Điện thoại / Zalo", Developer.PHONE) { open("tel:${Developer.PHONE}") }
                ContactRow(Icons.Outlined.Email, "Email", Developer.EMAIL) { open("mailto:${Developer.EMAIL}") }
                ContactRow(Icons.Outlined.Public, "Website", Developer.WEBSITE) { open("https://${Developer.WEBSITE}") }
                ContactRow(Icons.Outlined.Place, "Địa chỉ", Developer.ADDRESS) {
                    open("geo:0,0?q=" + android.net.Uri.encode(Developer.ADDRESS))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { open("tel:${Developer.PHONE}") }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp)) {
                        Text("Gọi hỗ trợ", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = { open("https://zalo.me/${Developer.PHONE}") }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp)) {
                        Text("Nhắn Zalo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text(
            "© 2026 ${Developer.COMPANY}",
            style = MaterialTheme.typography.bodySmall,
            color = cs.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
    }
}

/** Nhắc cấp quyền cần cho chuông báo thức (chỉ hiện khi máy đang chặn). */
@Composable
private fun AlarmPermissionCard() {
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }
    val needExact = remember(tick) {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    }
    val needFullScreen = remember(tick) {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            !context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
    }
    if (!needExact && !needFullScreen) return
    val cs = MaterialTheme.colorScheme
    fun open(action: String) {
        runCatching {
            context.startActivity(Intent(action, Uri.parse("package:" + context.packageName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
    Surface(shape = RoundedCornerShape(18.dp), color = cs.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cấp quyền cho chuông báo thức", fontWeight = FontWeight.Bold, color = cs.onPrimaryContainer)
            Text(
                "Để nhắc lịch đổ chuông đúng giờ và hiện màn hình chuông khi máy đang khoá, hãy cho phép các quyền dưới đây.",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onPrimaryContainer,
            )
            if (needExact) {
                Button(onClick = { open(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM) }) { Text("Cho phép báo đúng giờ") }
            }
            if (needFullScreen) {
                Button(onClick = { open(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT) }) { Text("Cho phép hiện chuông toàn màn hình") }
            }
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(cs.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = cs.primary) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}
