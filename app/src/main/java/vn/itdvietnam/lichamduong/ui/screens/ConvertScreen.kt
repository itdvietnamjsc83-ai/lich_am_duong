package vn.itdvietnam.lichamduong.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.itdvietnam.lichamduong.lunar.CanChi
import vn.itdvietnam.lichamduong.lunar.DayInfo
import vn.itdvietnam.lichamduong.lunar.LunarCalendar
import vn.itdvietnam.lichamduong.lunar.LunarDate
import vn.itdvietnam.lichamduong.ui.components.InfoPill
import vn.itdvietnam.lichamduong.ui.components.NumberStepper
import vn.itdvietnam.lichamduong.ui.components.SectionLabel
import vn.itdvietnam.lichamduong.ui.components.SegmentedChoice
import vn.itdvietnam.lichamduong.ui.components.wrap
import vn.itdvietnam.lichamduong.ui.theme.NumberFont
import java.time.LocalDate
import java.time.YearMonth

private data class ConvertResult(
    val ok: Boolean,
    val main: String = "",
    val sub: String = "",
    val canChi: String = "",
    val tags: List<String> = emptyList(),
    val error: String = "",
    val solar: LocalDate? = null,
    val lunar: LunarDate? = null,
)

/**
 * Đổi ngày dương → âm và âm → dương.
 * [onCreateReminder] nhận ngày âm của kết quả để tạo nhắc lịch theo âm lịch.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConvertScreen(
    onCreateReminder: (LunarDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val todayLunar = LunarCalendar.solarToLunar(today)
    var solarToLunar by rememberSaveable { mutableStateOf(true) }
    var sd by rememberSaveable { mutableIntStateOf(today.dayOfMonth) }
    var sm by rememberSaveable { mutableIntStateOf(today.monthValue) }
    var sy by rememberSaveable { mutableIntStateOf(today.year) }
    var ld by rememberSaveable { mutableIntStateOf(todayLunar.day) }
    var lm by rememberSaveable { mutableIntStateOf(todayLunar.month) }
    var ly by rememberSaveable { mutableIntStateOf(todayLunar.year) }
    var leap by rememberSaveable { mutableStateOf(todayLunar.leap) }

    val leapMonth = LunarCalendar.leapMonthOf(ly)
    val leapAvailable = leapMonth == lm
    val useLeap = leap && leapAvailable

    val daysInSolarMonth = YearMonth.of(sy, sm).lengthOfMonth()
    val solarDay = minOf(sd, daysInSolarMonth)

    val result: ConvertResult = if (solarToLunar) {
        val date = LocalDate.of(sy, sm, solarDay)
        val info = DayInfo.of(date)
        ConvertResult(
            ok = true,
            main = CanChi.lunarDateText(info.lunar),
            sub = "Năm ${info.yearCanChi} (${info.lunar.year}) · ${CanChi.lunarShort(info.lunar)}/${info.lunar.year} âm lịch",
            canChi = "Ngày ${info.dayCanChi} · Tháng ${info.monthCanChi} · Năm ${info.yearCanChi}",
            tags = listOf(
                "${info.weekday}, ${info.solarText}",
                if (info.lunarMonthLength == 30) "Tháng đủ (30 ngày)" else "Tháng thiếu (29 ngày)",
            ),
            solar = date,
            lunar = info.lunar,
        )
    } else {
        val date = LunarCalendar.lunarToSolar(ld, lm, ly, useLeap)
        if (date == null) {
            ConvertResult(
                ok = false,
                error = "Tháng ${CanChi.MONTH_NAMES[lm]}${if (useLeap) " nhuận" else ""} năm $ly là tháng thiếu, chỉ có 29 ngày. Hãy chọn ngày 29 trở xuống.",
            )
        } else {
            val info = DayInfo.of(date)
            ConvertResult(
                ok = true,
                main = "${info.weekday}, ${info.solarText}",
                sub = CanChi.lunarDateText(info.lunar) + " năm " + info.yearCanChi,
                canChi = "Ngày ${info.dayCanChi} · Tháng ${info.monthCanChi} · Năm ${info.yearCanChi}",
                tags = listOf(if (info.lunarMonthLength == 30) "Tháng đủ (30 ngày)" else "Tháng thiếu (29 ngày)"),
                solar = date,
                lunar = info.lunar,
            )
        }
    }

    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SegmentedChoice(
            options = listOf("Dương → Âm", "Âm → Dương"),
            selectedIndex = if (solarToLunar) 0 else 1,
            onSelect = { solarToLunar = it == 0 },
        )
        SectionLabel(if (solarToLunar) "Nhập ngày dương lịch" else "Nhập ngày âm lịch")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (solarToLunar) {
                NumberStepper("Ngày", "$solarDay", { sd = wrap(solarDay + 1, 1, daysInSolarMonth) }, { sd = wrap(solarDay - 1, 1, daysInSolarMonth) }, Modifier.weight(1f))
                NumberStepper("Tháng", "$sm", { sm = wrap(sm + 1, 1, 12) }, { sm = wrap(sm - 1, 1, 12) }, Modifier.weight(1f))
                NumberStepper("Năm", "$sy", { sy = minOf(2100, sy + 1) }, { sy = maxOf(1900, sy - 1) }, Modifier.weight(1f))
            } else {
                NumberStepper("Ngày âm", "$ld", { ld = wrap(ld + 1, 1, 30) }, { ld = wrap(ld - 1, 1, 30) }, Modifier.weight(1f))
                NumberStepper("Tháng âm", "$lm", { lm = wrap(lm + 1, 1, 12) }, { lm = wrap(lm - 1, 1, 12) }, Modifier.weight(1f))
                NumberStepper("Năm", "$ly", { ly = minOf(2100, ly + 1) }, { ly = maxOf(1900, ly - 1) }, Modifier.weight(1f))
            }
        }
        if (!solarToLunar) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (leapMonth != 0) "Năm $ly có tháng $leapMonth nhuận" else "Năm $ly không có tháng nhuận",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = useLeap,
                    onClick = { leap = !leap },
                    enabled = leapAvailable,
                    label = { Text("Tháng nhuận", fontWeight = FontWeight.SemiBold) },
                )
            }
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            FilledTonalIconButton(
                onClick = {
                    if (solarToLunar) {
                        result.lunar?.let { ld = it.day; lm = it.month; ly = it.year; leap = it.leap }
                    } else {
                        result.solar?.let { sd = it.dayOfMonth; sm = it.monthValue; sy = it.year }
                    }
                    solarToLunar = !solarToLunar
                },
            ) {
                Icon(Icons.Filled.SwapVert, contentDescription = "Đảo chiều, dùng kết quả làm ngày nhập")
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = cs.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel(if (solarToLunar) "Kết quả âm lịch" else "Kết quả dương lịch")
                if (result.ok) {
                    Text(result.main, fontFamily = NumberFont, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 32.sp, color = cs.primary)
                    Text(result.sub, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(result.canChi, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        result.tags.forEach { InfoPill(it) }
                    }
                } else {
                    Text(result.error, color = cs.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Button(
            onClick = { result.lunar?.let(onCreateReminder) },
            enabled = result.ok,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Outlined.NotificationsActive, contentDescription = null)
            Text("  Đặt nhắc lịch cho ngày này", fontWeight = FontWeight.SemiBold)
        }
    }
}
