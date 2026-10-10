package vn.itdvietnam.lichamduong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.itdvietnam.lichamduong.lunar.CanChi
import vn.itdvietnam.lichamduong.reminder.CalendarType
import vn.itdvietnam.lichamduong.reminder.Recurrence
import vn.itdvietnam.lichamduong.reminder.Reminder
import vn.itdvietnam.lichamduong.reminder.RepeatType
import vn.itdvietnam.lichamduong.ui.components.ChoiceChips
import vn.itdvietnam.lichamduong.ui.components.NumberStepper
import vn.itdvietnam.lichamduong.ui.components.SectionLabel
import vn.itdvietnam.lichamduong.ui.components.SegmentedChoice
import vn.itdvietnam.lichamduong.ui.components.wrap
import vn.itdvietnam.lichamduong.ui.theme.NumberFont
import java.time.LocalDate
import java.time.YearMonth

/** Tạo / sửa nhắc lịch: chọn âm hay dương, kiểu lặp, ngày, giờ báo; xem trước các lần nhắc. */
@Composable
fun ReminderEditScreen(
    initial: Reminder,
    onSave: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var r by remember(initial) { mutableStateOf(initial) }
    val cs = MaterialTheme.colorScheme
    val lunar = r.calendar == CalendarType.LUNAR
    val maxDay = when {
        lunar -> 30
        r.repeat == RepeatType.MONTHLY -> 31
        r.repeat == RepeatType.ONCE -> YearMonth.of(r.year, r.month).lengthOfMonth()
        else -> YearMonth.of(2028, r.month).lengthOfMonth() // năm nhuận để cho phép 29/2
    }
    val day = minOf(r.day, maxDay)
    val today = LocalDate.now()
    val preview = remember(r) { Recurrence.nextOccurrences(r.copy(day = day), today, 4) }

    Column(modifier) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Đóng") }
            Text(
                if (initial.id == 0L) "Nhắc lịch mới" else "Sửa nhắc lịch",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (initial.id != 0L) {
                IconButton(onClick = { onDelete(initial) }) { Icon(Icons.Outlined.Delete, contentDescription = "Xoá nhắc lịch") }
            }
            TextButton(onClick = { onSave(r.copy(day = day)) }) { Text("Lưu", fontWeight = FontWeight.Bold) }
        }

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = r.title,
                onValueChange = { r = r.copy(title = it) },
                label = { Text("Tên nhắc lịch") },
                placeholder = { Text("Ví dụ: Giỗ ông nội") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel("Theo lịch")
                SegmentedChoice(
                    options = listOf("Âm lịch", "Dương lịch"),
                    selectedIndex = if (lunar) 0 else 1,
                    onSelect = { r = r.copy(calendar = if (it == 0) CalendarType.LUNAR else CalendarType.SOLAR) },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel("Lặp lại")
                SegmentedChoice(
                    options = RepeatType.entries.map { it.label },
                    selectedIndex = r.repeat.ordinal,
                    onSelect = { r = r.copy(repeat = RepeatType.entries[it]) },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel(if (r.repeat == RepeatType.MONTHLY) "Ngày trong tháng" else "Ngày tháng")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberStepper(
                        if (lunar) "Ngày âm" else "Ngày", "$day",
                        { r = r.copy(day = wrap(day + 1, 1, maxDay)) },
                        { r = r.copy(day = wrap(day - 1, 1, maxDay)) },
                        Modifier.weight(1f),
                    )
                    if (r.repeat != RepeatType.MONTHLY) {
                        NumberStepper(
                            if (lunar) "Tháng âm" else "Tháng", "${r.month}",
                            { r = r.copy(month = wrap(r.month + 1, 1, 12)) },
                            { r = r.copy(month = wrap(r.month - 1, 1, 12)) },
                            Modifier.weight(1f),
                        )
                    }
                    if (r.repeat == RepeatType.ONCE) {
                        NumberStepper(
                            "Năm", "${r.year}",
                            { r = r.copy(year = minOf(2100, r.year + 1)) },
                            { r = r.copy(year = maxOf(today.year - 1, r.year - 1)) },
                            Modifier.weight(1f),
                        )
                    }
                }
                if (r.repeat == RepeatType.MONTHLY) {
                    val second = r.extraDays.firstOrNull()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (second == null) "Thêm một ngày nữa trong tháng" else "Ngày thứ hai: ${if (lunar) CanChi.lunarDayName(second) else "ngày $second"}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Switch(
                            checked = second != null,
                            onCheckedChange = { on -> r = r.copy(extraDays = if (on) listOf(if (day == 15) 1 else 15) else emptyList()) },
                        )
                    }
                    if (second != null) {
                        NumberStepper(
                            "Ngày thứ hai", "$second",
                            { r = r.copy(extraDays = listOf(wrap(second + 1, 1, maxDay))) },
                            { r = r.copy(extraDays = listOf(wrap(second - 1, 1, maxDay))) },
                            Modifier.fillMaxWidth(0.5f),
                        )
                    }
                }
                Text(r.copy(day = day).ruleText(), color = cs.primary, fontWeight = FontWeight.SemiBold)
                if (lunar) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(cs.surfaceVariant)
                            .padding(12.dp),
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = cs.onSurfaceVariant)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            buildString {
                                append("Tháng âm thiếu (29 ngày) sẽ nhắc vào ngày 29 nếu bạn chọn ngày 30.")
                                if (r.repeat == RepeatType.YEARLY) {
                                    append("\nNăm có tháng nhuận trùng tháng đã chọn: nhắc ở tháng chính, không nhắc lại ở tháng nhuận.")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel("Nhắc trước")
                ChoiceChips(
                    options = listOf(0, 1, 3, 7),
                    selected = r.beforeDays,
                    label = { if (it == 0) "Đúng ngày" else "Trước $it ngày" },
                    onSelect = { r = r.copy(beforeDays = it) },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel("Giờ báo")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberStepper("Giờ", "%02d".format(r.hour), { r = r.copy(hour = wrap(r.hour + 1, 0, 23)) }, { r = r.copy(hour = wrap(r.hour - 1, 0, 23)) }, Modifier.weight(1f))
                    NumberStepper("Phút", "%02d".format(r.minute), { r = r.copy(minute = (r.minute + 15) % 60) }, { r = r.copy(minute = (r.minute + 45) % 60) }, Modifier.weight(1f))
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cs.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { liveRegion = LiveRegionMode.Polite },
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Các lần nhắc sắp tới", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (preview.isEmpty()) {
                        Text("Ngày đã chọn đã qua. Hãy chọn năm khác hoặc bật lặp lại.", color = cs.onSurfaceVariant)
                    }
                    preview.forEach { date ->
                        val alarm = date.minusDays(r.beforeDays.toLong())
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(cs.surfaceContainerHigh)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.width(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${date.dayOfMonth}", fontFamily = NumberFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = cs.primary)
                                Text("th ${date.monthValue}", fontSize = 11.sp, color = cs.onSurfaceVariant)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(solarWithWeekday(date) + " · " + lunarOf(date), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Báo lúc %02d:%02d".format(r.hour, r.minute) +
                                        (if (r.beforeDays > 0) " ngày ${alarm.dayOfMonth}/${alarm.monthValue}/${alarm.year}" else " đúng ngày") +
                                        " · " + countdownText(date, today),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = cs.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { onSave(r.copy(day = day)) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
            ) { Text("Lưu nhắc lịch", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(16.dp))
        }
    }
}
