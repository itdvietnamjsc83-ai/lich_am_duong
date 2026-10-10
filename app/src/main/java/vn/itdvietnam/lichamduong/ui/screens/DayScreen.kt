package vn.itdvietnam.lichamduong.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.itdvietnam.lichamduong.lunar.CanChi
import vn.itdvietnam.lichamduong.lunar.DayInfo
import vn.itdvietnam.lichamduong.ui.components.InfoPill
import vn.itdvietnam.lichamduong.ui.theme.NumberFont

/** Chi tiết một ngày: kiểu lịch bloc, số ngày rất to, âm lịch, can chi, giờ hoàng đạo. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayDetail(
    info: DayInfo,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onAddReminder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Tháng ${info.date.monthValue}, ${info.date.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(onClick = onToday) { Text("Hôm nay", fontWeight = FontWeight.SemiBold) }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = onPrev, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Ngày trước")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(info.weekday, style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                Text(
                    "${info.date.dayOfMonth}",
                    fontFamily = NumberFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 120.sp,
                    lineHeight = 124.sp,
                    color = cs.primary,
                )
                Text("Dương lịch ${info.solarText}", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
            FilledTonalIconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ngày sau")
            }
        }

        Surface(shape = RoundedCornerShape(20.dp), color = cs.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${info.lunar.day}", fontFamily = NumberFont, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 46.sp)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "tháng " + CanChi.MONTH_NAMES[info.lunar.month] + (if (info.lunar.leap) " nhuận" else "") + " âm lịch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CanChiBox("Ngày", info.dayCanChi, Modifier.weight(1f))
                    CanChiBox("Tháng", info.monthCanChi, Modifier.weight(1f))
                    CanChiBox("Năm", info.yearCanChi, Modifier.weight(1f))
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    InfoPill("Tiết khí: ${info.tietKhi}")
                    InfoPill(info.moonPhase)
                    InfoPill(if (info.lunarMonthLength == 30) "Tháng đủ (30 ngày)" else "Tháng thiếu (29 ngày)")
                }
            }
        }

        info.events.forEach { ev ->
            Text(
                ev,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cs.primaryContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                color = cs.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text("Giờ hoàng đạo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        info.gioHoangDao.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { g ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, cs.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(g.chi, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                        Text(g.range, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                }
            }
        }

        Button(
            onClick = onAddReminder,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) { Text("+ Thêm nhắc lịch cho ngày này", fontWeight = FontWeight.SemiBold) }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CanChiBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}
