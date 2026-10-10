package vn.itdvietnam.lichamduong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.itdvietnam.lichamduong.lunar.CanChi
import vn.itdvietnam.lichamduong.lunar.DayInfo
import vn.itdvietnam.lichamduong.lunar.LunarCalendar
import vn.itdvietnam.lichamduong.lunar.LunarDate
import vn.itdvietnam.lichamduong.ui.theme.NumberFont
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class MonthCell(val info: DayInfo, val inMonth: Boolean) {
    val description: String
        get() = buildString {
            append(info.weekday).append(" ").append(info.solarText).append(", ")
            append(CanChi.lunarDateText(info.lunar)).append(" âm lịch")
            if (info.events.isNotEmpty()) append(", ").append(info.events.joinToString(", "))
        }
}

fun monthCells(month: YearMonth): List<MonthCell> {
    val first = month.atDay(1)
    val offset = first.dayOfWeek.value - 1 // tuần bắt đầu từ Thứ Hai
    val start = first.minusDays(offset.toLong())
    val total = ((offset + month.lengthOfMonth() + 6) / 7) * 7
    return (0 until total).map { i ->
        val date = start.plusDays(i.toLong())
        MonthCell(DayInfo.of(date), YearMonth.from(date) == month)
    }
}

private fun monthLabel(d: LunarDate) = "${d.month}" + if (d.leap) " nhuận" else ""

fun lunarMonthSubtitle(month: YearMonth): String {
    val a = LunarCalendar.solarToLunar(month.atDay(1))
    val b = LunarCalendar.solarToLunar(month.atEndOfMonth())
    val months = if (a.month == b.month && a.leap == b.leap) "Tháng ${monthLabel(a)} âm lịch"
    else "Tháng ${monthLabel(a)} – ${monthLabel(b)} âm lịch"
    val years = if (a.year == b.year) "Năm ${CanChi.year(a.year)}"
    else "Năm ${CanChi.year(a.year)} – ${CanChi.year(b.year)}"
    return "$months · $years"
}

@Composable
fun MonthHeader(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onSettings: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                "Tháng ${month.monthValue}, ${month.year}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                lunarMonthSubtitle(month),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Tháng trước")
        }
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Tháng sau")
        }
        OutlinedButton(onClick = onToday, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)) {
            Text("Hôm nay", fontWeight = FontWeight.SemiBold)
        }
        if (onSettings != null) {
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = "Cài đặt và giới thiệu")
            }
        }
    }
}

@Composable
fun MonthGrid(
    month: YearMonth,
    selected: LocalDate,
    today: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
    cellHeight: Dp = 62.dp,
) {
    val cells = remember(month) { monthCells(month) }
    Column(
        modifier = modifier.pointerInput(month) {
            var total = 0f
            detectHorizontalDragGestures(
                onDragStart = { total = 0f },
                onDragEnd = {
                    if (total > 120f) onPrevMonth() else if (total < -120f) onNextMonth()
                    total = 0f
                },
            ) { _, dragAmount -> total += dragAmount }
        },
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            CanChi.WEEKDAYS_SHORT.forEachIndexed { i, label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (i == 6) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (i == 6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { cell ->
                    DayCell(
                        cell = cell,
                        isToday = cell.info.date == today,
                        isSelected = cell.info.date == selected,
                        onTap = { onSelect(cell.info.date) },
                        height = cellHeight,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    cell: MonthCell,
    isToday: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    val info = cell.info
    val special = info.isSpecialLunarDay
    val numberColor = when {
        isToday -> cs.onPrimary
        info.date.dayOfWeek == DayOfWeek.SUNDAY -> cs.primary
        else -> cs.onSurface
    }
    val lunarColor = when {
        isToday -> cs.onPrimary
        special -> cs.primary
        else -> cs.onSurfaceVariant
    }
    val description = cell.description
    val select = onTap
    Box(
        modifier = modifier
            .height(height)
            .alpha(if (cell.inMonth) 1f else 0.38f)
            .clip(shape)
            .background(if (isToday) cs.primary else Color.Transparent)
            .then(if (isSelected && !isToday) Modifier.border(2.dp, cs.primary, shape) else Modifier)
            .clickable(onClick = select)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
                onClick(label = null, action = { select(); true })
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${info.date.dayOfMonth}",
                fontSize = 20.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
                color = numberColor,
                lineHeight = 22.sp,
            )
            Text(
                if (info.lunar.day == 1) "1/${info.lunar.month}" else "${info.lunar.day}",
                fontSize = 12.sp,
                fontWeight = if (special) FontWeight.Bold else FontWeight.Normal,
                color = lunarColor,
                lineHeight = 14.sp,
            )
        }
        if (info.events.isNotEmpty()) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 5.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (isToday) cs.onPrimary else cs.secondary),
            )
        }
    }
}

/** Thẻ tóm tắt ngày đang chọn, hiện dưới lưới tháng trên điện thoại thanh. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectedDayCard(info: DayInfo, onOpenDay: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = cs.surfaceVariant) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cs.primary),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("${info.lunar.day}", fontFamily = NumberFont, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = cs.onPrimary, lineHeight = 32.sp)
                    Text("tháng ${info.lunar.month}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = cs.onPrimary)
                }
                Spacer(Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("${info.weekday}, ${info.solarText}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(CanChi.lunarDateText(info.lunar) + " âm lịch", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Ngày ${info.dayCanChi} · Tháng ${info.monthCanChi} · Năm ${info.yearCanChi}",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant,
                    )
                }
            }
            info.events.forEach { ev ->
                Text(
                    ev,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(cs.surfaceContainerHigh)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = cs.primary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text("Giờ hoàng đạo", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                info.gioHoangDao.forEach { g ->
                    Text(
                        "${g.chi} ${g.range}",
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(cs.surfaceContainerHigh)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            TextButton(onClick = onOpenDay) {
                Text("Xem chi tiết ngày →", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** Danh sách sự kiện trong tháng (dùng ở ngăn trái của màn hình mở rộng). */
@Composable
fun MonthEventList(month: YearMonth, selected: LocalDate, onSelect: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    val cells = remember(month) { monthCells(month).filter { it.inMonth && it.info.events.isNotEmpty() } }
    val cs = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Sự kiện trong tháng", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, bottom = 2.dp))
        if (cells.isEmpty()) {
            Text("Không có sự kiện", color = cs.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp))
        }
        cells.forEach { cell ->
            val isSel = cell.info.date == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSel) cs.surfaceVariant else Color.Transparent)
                    .then(if (isSel) Modifier.border(2.dp, cs.primary, RoundedCornerShape(12.dp)) else Modifier)
                    .clickable { onSelect(cell.info.date) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.width(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${cell.info.date.dayOfMonth}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(CanChi.lunarShort(cell.info.lunar) + " âm", fontSize = 11.sp, color = cs.onSurfaceVariant)
                }
                Spacer(Modifier.width(10.dp))
                Text(cell.info.events.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
