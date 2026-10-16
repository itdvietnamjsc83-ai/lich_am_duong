package vn.itdvietnam.lichamduong.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.itdvietnam.lichamduong.lunar.CanChi
import vn.itdvietnam.lichamduong.routine.Routine
import vn.itdvietnam.lichamduong.routine.RoutineSchedule
import vn.itdvietnam.lichamduong.routine.RoutineScheduler
import vn.itdvietnam.lichamduong.ui.components.ChoiceChips
import vn.itdvietnam.lichamduong.ui.components.SectionLabel
import vn.itdvietnam.lichamduong.ui.components.SegmentedChoice
import vn.itdvietnam.lichamduong.ui.components.TimePickDialog
import vn.itdvietnam.lichamduong.ui.theme.NumberFont
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/** Mẫu có sẵn để tạo nhanh khi chưa có việc nào. */
private val TEMPLATES = listOf(
    Triple("Uống thuốc", listOf(420, 720, 1140), "3 lần/ngày: 7h, 12h, 19h"),
    Triple("Cho con ăn", RoutineSchedule.cycle(180, 360, 1260), "Mỗi 3 giờ, 6h – 21h"),
    Triple("Đổ nước thải", listOf(1050), "17h30 mỗi ngày"),
)

/** Danh sách "Hôm nay" + các việc lặp hằng ngày. */
@Composable
fun RoutinesScreen(
    routines: List<Routine>,
    done: Set<String>,
    selectedId: Long?,
    onToggleDone: (String) -> Unit,
    onOpen: (Routine) -> Unit,
    onToggle: (Routine, Boolean) -> Unit,
    onCreate: (Routine?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val today = LocalDate.now()
    val now = LocalTime.now().let { it.hour * 60 + it.minute }
    val slots = remember(routines, today) { RoutineSchedule.slotsOn(routines, today) }
    val nextIndex = slots.indexOfFirst { it.minute >= now }
    val doneCount = slots.count { it.key in done }

    Box(modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = cs.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Hôm nay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(
                                CanChi.WEEKDAYS[today.dayOfWeek.value - 1] + ", ${today.dayOfMonth}/${today.monthValue}" +
                                    if (slots.isNotEmpty()) " · đã xong $doneCount/${slots.size}" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant,
                            )
                        }
                        if (slots.isEmpty()) {
                            Text("Hôm nay không có việc lặp nào.", color = cs.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        slots.forEachIndexed { i, slot ->
                            val isDone = slot.key in done
                            val isNext = i == nextIndex
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isNext) cs.surfaceContainerHigh else Color.Transparent)
                                    .then(if (isNext) Modifier.border(2.dp, cs.primary, RoundedCornerShape(14.dp)) else Modifier)
                                    .alpha(if (isDone) 0.55f else 1f)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    RoutineSchedule.format(slot.minute),
                                    fontFamily = NumberFont, fontWeight = FontWeight.Bold, fontSize = 19.sp,
                                    color = if (isNext) cs.primary else cs.onSurface,
                                    modifier = Modifier.width(60.dp),
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        slot.routine.title,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = if (isDone) TextDecoration.LineThrough else null,
                                    )
                                    Text(
                                        listOf(slot.routine.who, if (slot.routine.ring) "Đổ chuông" else "Thông báo").filter { it.isNotBlank() }.joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = cs.onSurfaceVariant,
                                    )
                                }
                                if (isNext && !isDone) {
                                    Text(
                                        "Tiếp theo",
                                        modifier = Modifier.clip(RoundedCornerShape(50)).background(cs.primary).padding(horizontal = 8.dp, vertical = 3.dp),
                                        color = cs.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                }
                                IconButton(onClick = { onToggleDone(slot.key) }) {
                                    Box(
                                        Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (isDone) cs.primary else Color.Transparent)
                                            .border(2.dp, if (isDone) cs.primary else cs.outline, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (isDone) Icon(Icons.Filled.Check, contentDescription = null, tint = cs.onPrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Việc lặp", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${routines.size} việc", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
            }
            if (routines.isEmpty()) {
                item {
                    Text("Chưa có việc lặp nào. Chọn nhanh một mẫu để bắt đầu:", color = cs.onSurfaceVariant)
                }
                items(TEMPLATES) { (title, times, desc) ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        onClick = { onCreate(Routine(0L, title, times = times, startDate = LocalDate.now())) },
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = cs.primary)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(title, fontWeight = FontWeight.Bold)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            items(routines, key = { it.id }) { r ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth().alpha(if (r.enabled) 1f else 0.55f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(if (r.id == selectedId) 2.dp else 1.dp, if (r.id == selectedId) cs.primary else cs.outlineVariant),
                ) {
                    Row(Modifier.clickable { onOpen(r) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(cs.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (r.ring) Icons.Outlined.Alarm else Icons.Outlined.Notifications,
                                contentDescription = if (r.ring) "Đổ chuông" else "Thông báo",
                                tint = cs.primary,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(r.displayTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(r.timesText(), color = cs.primary, fontWeight = FontWeight.SemiBold)
                            Text(r.ruleText(), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                        }
                        Switch(checked = r.enabled, onCheckedChange = { onToggle(r, it) })
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { onCreate(null) },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Thêm việc lặp", fontWeight = FontWeight.Bold) },
            containerColor = cs.primary,
            contentColor = cs.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
}

private val TITLE_CHIPS = listOf("Uống thuốc", "Cho con ăn", "Cho con bú", "Đổ nước thải", "Tập thể dục", "Uống nước")
private val PRESETS = listOf(
    "1 lần: 7h" to listOf(420),
    "2 lần: 7h, 19h" to listOf(420, 1140),
    "3 lần: 7h, 12h, 19h" to listOf(420, 720, 1140),
    "4 lần: 7h, 11h, 15h, 19h" to listOf(420, 660, 900, 1140),
)
private val WEEKDAY_LABELS = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

/** Thêm / sửa việc lặp hằng ngày, nhập nhanh nhiều giờ. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoutineEditScreen(
    initial: Routine,
    onSave: (Routine) -> Unit,
    onDelete: (Routine) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    var r by remember(initial) { mutableStateOf(initial) }
    var quick by remember(initial) { mutableStateOf("") }
    var every by remember { mutableIntStateOf(3) }
    var from by remember { mutableIntStateOf(360) }
    var to by remember { mutableIntStateOf(1260) }
    var picking by remember { mutableStateOf<Int?>(null) } // null: đóng, -1: thêm mới, >=0: sửa giờ thứ i
    var pickDays by remember(initial) { mutableStateOf(initial.days.size < 7) }

    fun addTimes(list: List<Int>) { r = r.copy(times = RoutineSchedule.normalize(r.times + list)) }
    val generated = RoutineSchedule.cycle(every * 60, from, to)
    val endDays = r.endDate?.let { ChronoUnit.DAYS.between(r.startDate, it).toInt() + 1 }

    Column(modifier) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Đóng") }
            Text(
                if (initial.id == 0L) "Việc lặp mới" else "Sửa việc lặp",
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f),
            )
            if (initial.id != 0L) {
                IconButton(onClick = { onDelete(initial) }) { Icon(Icons.Outlined.Delete, contentDescription = "Xoá việc lặp") }
            }
            TextButton(onClick = { onSave(r) }, enabled = r.times.isNotEmpty()) { Text("Lưu", fontWeight = FontWeight.Bold) }
        }

        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = r.title, onValueChange = { r = r.copy(title = it) },
                    label = { Text("Tên công việc") }, placeholder = { Text("Ví dụ: Uống thuốc huyết áp") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TITLE_CHIPS.forEach { t -> FilterChip(selected = r.title == t, onClick = { r = r.copy(title = t) }, label = { Text(t) }) }
                }
            }
            OutlinedTextField(
                value = r.who, onValueChange = { r = r.copy(who = it) },
                label = { Text("Dành cho (không bắt buộc)") }, placeholder = { Text("Bố, Mẹ, Bé Na…") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )

            Surface(shape = RoundedCornerShape(18.dp), color = cs.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Giờ trong ngày", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${r.times.size} lần mỗi ngày", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        r.times.forEachIndexed { i, m ->
                            Row(
                                Modifier
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(cs.surfaceContainerHigh)
                                    .border(1.dp, cs.outlineVariant, RoundedCornerShape(22.dp))
                                    .clickable(onClickLabel = "Đổi giờ") { picking = i }
                                    .padding(start = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(RoutineSchedule.format(m), fontFamily = NumberFont, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                IconButton(onClick = { r = r.copy(times = r.times - m) }, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Filled.Close, contentDescription = "Xoá giờ ${RoutineSchedule.format(m)}", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        OutlinedButton(onClick = { picking = -1 }) { Text("+ Chọn giờ", fontWeight = FontWeight.Bold) }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = quick, onValueChange = { quick = it },
                            label = { Text("Gõ nhanh nhiều giờ") }, placeholder = { Text("7h 11h30 19h") },
                            singleLine = true, modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { addTimes(RoutineSchedule.parseTimes(quick)); quick = "" }),
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { addTimes(RoutineSchedule.parseTimes(quick)); quick = "" }, enabled = quick.isNotBlank()) { Text("Thêm") }
                    }

                    SectionLabel("Chọn nhanh")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PRESETS.forEach { (label, list) ->
                            FilterChip(selected = r.times == list, onClick = { r = r.copy(times = list) }, label = { Text(label) })
                        }
                    }

                    SectionLabel("Tạo theo chu kỳ")
                    FlowRow(verticalArrangement = Arrangement.Center, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Mỗi", modifier = Modifier.align(Alignment.CenterVertically))
                        MiniStepper("$every giờ", { every = (every - 1).coerceAtLeast(1) }, { every = (every + 1).coerceAtMost(12) })
                        Text("từ", modifier = Modifier.align(Alignment.CenterVertically))
                        MiniStepper(RoutineSchedule.format(from), { from = (from - 30).coerceAtLeast(0) }, { from = (from + 30).coerceAtMost(to) })
                        Text("đến", modifier = Modifier.align(Alignment.CenterVertically))
                        MiniStepper(RoutineSchedule.format(to), { to = (to - 30).coerceAtLeast(from) }, { to = (to + 30).coerceAtMost(1410) })
                    }
                    OutlinedButton(onClick = { addTimes(generated) }, enabled = generated.isNotEmpty()) {
                        Text("Tạo ${generated.size} giờ: " + generated.joinToString(", ") { RoutineSchedule.format(it) }, fontWeight = FontWeight.SemiBold)
                    }
                    if (r.times.isNotEmpty()) {
                        TextButton(onClick = { r = r.copy(times = emptyList()) }) { Text("Xoá hết giờ") }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Lặp vào")
                SegmentedChoice(
                    options = listOf("Hằng ngày", "Chọn thứ"),
                    selectedIndex = if (pickDays) 1 else 0,
                    onSelect = { pickDays = it == 1; if (it == 0) r = r.copy(days = Routine.ALL_DAYS) },
                )
                if (pickDays) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        WEEKDAY_LABELS.forEachIndexed { i, label ->
                            val d = i + 1
                            val on = d in r.days
                            FilterChip(
                                selected = on,
                                onClick = { r = r.copy(days = if (on) r.days - d else r.days + d) },
                                label = { Text(label, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = cs.primary, selectedLabelColor = cs.onPrimary),
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Thời gian áp dụng")
                Row(
                    Modifier.fillMaxWidth().border(1.dp, cs.outlineVariant, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        val end = r.endDate
                        Text(
                            if (end == null) "Không có ngày kết thúc" else "Đến hết ngày ${end.dayOfMonth}/${end.monthValue}/${end.year}",
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Bắt đầu từ ${r.startDate.dayOfMonth}/${r.startDate.monthValue}/${r.startDate.year}",
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = r.endDate != null,
                        onCheckedChange = { on -> r = r.copy(endDate = if (on) r.startDate.plusDays(9) else null) },
                    )
                }
                if (endDays != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Trong ")
                        MiniStepper(
                            "$endDays ngày",
                            { r = r.copy(endDate = r.startDate.plusDays((endDays - 2).coerceAtLeast(0).toLong())) },
                            { r = r.copy(endDate = r.startDate.plusDays(endDays.coerceAtMost(364).toLong())) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Cách báo")
                SegmentedChoice(
                    options = listOf("Đổ chuông", "Thông báo"),
                    selectedIndex = if (r.ring) 0 else 1,
                    onSelect = { r = r.copy(ring = it == 0) },
                )
                Text(
                    if (r.ring) "Đến giờ, điện thoại đổ chuông và rung như báo thức (cả khi khoá máy, để im lặng), có nút Tắt và Báo lại."
                    else "Chỉ hiện thông báo kèm âm báo thông thường.",
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                )
                if (r.ring) {
                    SectionLabel("Báo lại sau")
                    ChoiceChips(listOf(5, 10, 15, 30), r.snoozeMinutes, { "$it phút" }, { r = r.copy(snoozeMinutes = it) })
                }
                OutlinedButton(
                    onClick = {
                        val n = RoutineScheduler.addToSystemClock(context, r)
                        Toast.makeText(
                            context,
                            if (n > 0) "Đã thêm $n báo thức vào Đồng hồ của máy" else "Máy không hỗ trợ thêm báo thức vào Đồng hồ",
                            Toast.LENGTH_LONG,
                        ).show()
                    },
                    enabled = r.times.isNotEmpty(),
                ) { Text("Thêm vào Đồng hồ của máy") }
                Text(
                    "Tuỳ chọn: tạo báo thức tương ứng trong ứng dụng Đồng hồ (lặp theo thứ, không tự hết hạn). Không bắt buộc — ứng dụng này đã tự báo.",
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                )
            }

            OutlinedTextField(
                value = r.note, onValueChange = { r = r.copy(note = it) },
                label = { Text("Ghi chú (hiện khi báo)") }, placeholder = { Text("Ví dụ: uống sau ăn, 1 viên") },
                modifier = Modifier.fillMaxWidth(),
            )

            val now = LocalTime.now().let { it.hour * 60 + it.minute }
            val left = if (RoutineSchedule.occursOn(r.copy(enabled = true), LocalDate.now())) r.times.filter { it > now } else emptyList()
            val total = RoutineSchedule.totalCount(r)
            Surface(
                shape = RoundedCornerShape(18.dp), color = cs.surfaceVariant,
                modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (r.times.isEmpty()) "Chưa có giờ nào — hãy thêm giờ"
                        else "Báo ${r.times.size} lần " + (if (r.days.size == 7) "mỗi ngày" else r.daysText().lowercase()) + ": " +
                            r.times.joinToString(", ") { RoutineSchedule.format(it) },
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        (if (left.isNotEmpty()) "Hôm nay còn ${left.size} lần: " + left.joinToString(", ") { RoutineSchedule.format(it) } else "Hôm nay không còn lần nào") +
                            (total?.let { " · tổng $it lần" } ?: ""),
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                    )
                }
            }

            Button(
                onClick = { onSave(r) },
                enabled = r.times.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
            ) { Text("Lưu việc lặp", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(16.dp))
        }
    }

    picking?.let { index ->
        val m = if (index >= 0) r.times.getOrNull(index) ?: 420 else (r.times.lastOrNull()?.plus(60)?.coerceAtMost(1380) ?: 420)
        TimePickDialog(
            title = if (index >= 0) "Đổi giờ" else "Thêm giờ",
            hour = m / 60, minute = m % 60,
            onDismiss = { picking = null },
            onPick = { h, mi ->
                val list = if (index >= 0) r.times.filterIndexed { i, _ -> i != index } else r.times
                r = r.copy(times = RoutineSchedule.normalize(list + (h * 60 + mi)))
                picking = null
            },
        )
    }
}

@Composable
private fun MiniStepper(value: String, onDec: () -> Unit, onInc: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.clip(RoundedCornerShape(12.dp)).border(1.dp, cs.outlineVariant, RoundedCornerShape(12.dp)).background(cs.surfaceContainerHigh),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onDec, contentPadding = PaddingValues(0.dp), modifier = Modifier.width(40.dp)) { Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        Text(value, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 2.dp))
        TextButton(onClick = onInc, contentPadding = PaddingValues(0.dp), modifier = Modifier.width(40.dp)) { Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
    }
}
