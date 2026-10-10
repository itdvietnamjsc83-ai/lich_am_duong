package vn.itdvietnam.lichamduong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.itdvietnam.lichamduong.lunar.CanChi
import vn.itdvietnam.lichamduong.lunar.LunarCalendar
import vn.itdvietnam.lichamduong.reminder.CalendarType
import vn.itdvietnam.lichamduong.reminder.Recurrence
import vn.itdvietnam.lichamduong.reminder.Reminder
import vn.itdvietnam.lichamduong.ui.components.SegmentedChoice
import vn.itdvietnam.lichamduong.ui.theme.NumberFont
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private data class ReminderRow(val reminder: Reminder, val next: LocalDate?)

fun countdownText(date: LocalDate, today: LocalDate = LocalDate.now()): String {
    val n = ChronoUnit.DAYS.between(today, date)
    return when (n) {
        0L -> "hôm nay"
        1L -> "ngày mai"
        else -> "còn $n ngày"
    }
}

fun solarWithWeekday(date: LocalDate): String =
    CanChi.WEEKDAYS_SHORT[date.dayOfWeek.value - 1] + ", ${date.dayOfMonth}/${date.monthValue}/${date.year}"

fun lunarOf(date: LocalDate): String = CanChi.lunarShort(LunarCalendar.solarToLunar(date)) + " âm"

@Composable
fun RemindersScreen(
    reminders: List<Reminder>,
    selectedId: Long?,
    onOpen: (Reminder) -> Unit,
    onToggle: (Reminder, Boolean) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    var filter by rememberSaveable { mutableIntStateOf(0) } // 0 tất cả, 1 âm, 2 dương
    val rows = remember(reminders, today) {
        reminders
            .map { ReminderRow(it, Recurrence.nextOccurrences(it, today, 1).firstOrNull()) }
            .sortedWith(compareBy<ReminderRow> { it.next == null }.thenBy { it.next })
    }
    val visible = rows.filter {
        when (filter) {
            1 -> it.reminder.calendar == CalendarType.LUNAR
            2 -> it.reminder.calendar == CalendarType.SOLAR
            else -> true
        }
    }
    val first = rows.firstOrNull { it.reminder.enabled && it.next != null }
    val cs = MaterialTheme.colorScheme

    Box(modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (first?.next != null) {
                item {
                    Surface(shape = RoundedCornerShape(18.dp), color = cs.primary, contentColor = cs.onPrimary) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(
                                Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(cs.onPrimary.copy(alpha = 0.18f)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text("${ChronoUnit.DAYS.between(today, first.next)}", fontFamily = NumberFont, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                Text("ngày", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text("Sắp tới gần nhất", style = MaterialTheme.typography.labelMedium)
                                Text(first.reminder.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(solarWithWeekday(first.next) + " · " + lunarOf(first.next), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            item {
                SegmentedChoice(
                    options = listOf("Tất cả", "Âm lịch", "Dương lịch"),
                    selectedIndex = filter,
                    onSelect = { filter = it },
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
            if (visible.isEmpty()) {
                item {
                    Text(
                        "Chưa có nhắc lịch. Bấm “Tạo nhắc” để thêm ngày giỗ, rằm, mùng 1 hoặc ngày kỷ niệm.",
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }
            items(visible, key = { it.reminder.id }) { row ->
                ReminderCard(
                    row = row,
                    selected = row.reminder.id == selectedId,
                    onOpen = { onOpen(row.reminder) },
                    onToggle = { onToggle(row.reminder, it) },
                )
            }
        }
        ExtendedFloatingActionButton(
            onClick = onCreate,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Tạo nhắc", fontWeight = FontWeight.Bold) },
            containerColor = cs.primary,
            contentColor = cs.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }
}

@Composable
private fun ReminderCard(row: ReminderRow, selected: Boolean, onOpen: () -> Unit, onToggle: (Boolean) -> Unit) {
    val r = row.reminder
    val cs = MaterialTheme.colorScheme
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (r.enabled) 1f else 0.55f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, if (selected) cs.primary else cs.outlineVariant),
    ) {
        Row(
            Modifier
                .clickable(onClick = onOpen)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (r.isLunar) cs.primaryContainer else cs.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (r.isLunar) Icons.Outlined.DarkMode else Icons.Outlined.WbSunny,
                    contentDescription = if (r.isLunar) "Âm lịch" else "Dương lịch",
                    tint = if (r.isLunar) cs.primary else cs.secondary,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(r.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(r.ruleText(), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                if (row.next != null) {
                    Text(solarWithWeekday(row.next) + " · " + countdownText(row.next), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(lunarOf(row.next) + " · " + r.alertText(), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                } else {
                    Text("Đã qua", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
            }
            Switch(checked = r.enabled, onCheckedChange = onToggle)
        }
    }
}
