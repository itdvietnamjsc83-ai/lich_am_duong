package vn.itdvietnam.lichamduong.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.itdvietnam.lichamduong.ui.theme.NumberFont

/** Nhóm nút chọn một trong nhiều (kiểu "segmented"). */
@Composable
fun SegmentedChoice(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceVariant)
                    .selectable(selected = selected, onClick = { onSelect(index) }, role = Role.RadioButton),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Dãy chip chọn một giá trị. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChoiceChips(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option), fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

/** Ô chọn số có nút ▲/▼ cỡ lớn, dễ bấm. */
@Composable
fun NumberStepper(
    label: String,
    value: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        IconButton(onClick = onIncrease) {
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Tăng $label")
        }
        Text(value, fontFamily = NumberFont, fontWeight = FontWeight.Bold, fontSize = 30.sp)
        IconButton(onClick = onDecrease) {
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Giảm $label")
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Nhãn bo tròn hiển thị thông tin ngắn. */
@Composable
fun InfoPill(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        style = MaterialTheme.typography.bodySmall,
    )
}

/** Vòng lặp giá trị trong khoảng [min]..[max]. */
fun wrap(value: Int, min: Int, max: Int): Int = when {
    value < min -> max
    value > max -> min
    else -> value
}

/**
 * Chọn mốc báo tự do: số ngày trước ngày nhắc (0–30) và giờ:phút bất kỳ.
 * Chạm vào giờ để mở mặt đồng hồ; nút ▲▼ để chỉnh nhanh từng giờ, từng phút.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayTimePicker(
    beforeDays: Int,
    hour: Int,
    minute: Int,
    title: String,
    onBeforeDays: (Int) -> Unit,
    onTime: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    var showClock by remember { mutableStateOf(false) }
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, cs.outlineVariant, RoundedCornerShape(16.dp))
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Ngày báo", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = { onBeforeDays((beforeDays - 1).coerceAtLeast(0)) }) {
                    Icon(Icons.Filled.Remove, contentDescription = "Bớt một ngày")
                }
                Text(
                    if (beforeDays == 0) "Đúng\nngày" else "Trước\n$beforeDays ngày",
                    modifier = Modifier.widthIn(min = 64.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                FilledTonalIconButton(onClick = { onBeforeDays((beforeDays + 1).coerceAtMost(30)) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Thêm một ngày")
                }
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, cs.outlineVariant, RoundedCornerShape(16.dp))
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onTime((hour + 1) % 24, minute) }) { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Tăng giờ") }
                TextButton(onClick = { showClock = true }) {
                    Text("%02d".format(hour), fontFamily = NumberFont, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                }
                IconButton(onClick = { onTime((hour + 23) % 24, minute) }) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Giảm giờ") }
            }
            Text(":", fontFamily = NumberFont, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onTime(hour, (minute + 1) % 60) }) { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Tăng phút") }
                TextButton(onClick = { showClock = true }) {
                    Text("%02d".format(minute), fontFamily = NumberFont, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                }
                IconButton(onClick = { onTime(hour, (minute + 59) % 60) }) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Giảm phút") }
            }
        }
    }
    if (showClock) {
        val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showClock = false },
            title = { Text(title) },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = { onTime(state.hour, state.minute); showClock = false }) { Text("Xong", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showClock = false }) { Text("Huỷ") } },
        )
    }
}

/** Hộp thoại chọn giờ trên mặt đồng hồ (24 giờ). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickDialog(title: String, hour: Int, minute: Int, onDismiss: () -> Unit, onPick: (Int, Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onPick(state.hour, state.minute) }) { Text("Xong", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } },
    )
}

/**
 * Dải thương hiệu nhà phát triển, luôn hiện ở cuối màn hình (trên thanh điều hướng).
 * Chạm vào để mở thông tin liên hệ.
 */
@Composable
fun BrandStrip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(cs.background)
            .border(width = 0.5.dp, color = cs.outlineVariant)
            .clickable(onClickLabel = "Xem thông tin nhà phát triển", onClick = onClick)
            .heightIn(min = 34.dp)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(androidx.compose.ui.graphics.Color.White)
                .padding(horizontal = 6.dp, vertical = 3.dp),
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(vn.itdvietnam.lichamduong.R.drawable.itd_logo),
                contentDescription = null,
                modifier = Modifier.heightIn(max = 16.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            androidx.compose.ui.text.buildAnnotatedString {
                append("Phát triển bởi ")
                pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = cs.onBackground))
                append("ITD Việt Nam")
                pop()
                append(" · 0988 095 867")
            },
            style = MaterialTheme.typography.labelMedium,
            color = cs.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
