package vn.itdvietnam.lichamduong.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import vn.itdvietnam.lichamduong.routine.Routine
import vn.itdvietnam.lichamduong.ui.components.BrandStrip
import vn.itdvietnam.lichamduong.ui.components.SegmentedChoice
import vn.itdvietnam.lichamduong.ui.screens.RoutineEditScreen
import vn.itdvietnam.lichamduong.ui.screens.RoutinesScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import vn.itdvietnam.lichamduong.lunar.DayInfo
import vn.itdvietnam.lichamduong.reminder.CalendarType
import vn.itdvietnam.lichamduong.reminder.Reminder
import vn.itdvietnam.lichamduong.reminder.ReminderNotifications
import vn.itdvietnam.lichamduong.reminder.RepeatType
import vn.itdvietnam.lichamduong.ui.screens.ConvertScreen
import vn.itdvietnam.lichamduong.ui.screens.DayDetail
import vn.itdvietnam.lichamduong.ui.screens.MonthEventList
import vn.itdvietnam.lichamduong.ui.screens.MonthGrid
import vn.itdvietnam.lichamduong.ui.screens.MonthHeader
import vn.itdvietnam.lichamduong.ui.screens.ReminderEditScreen
import vn.itdvietnam.lichamduong.ui.screens.RemindersScreen
import vn.itdvietnam.lichamduong.ui.screens.SelectedDayCard
import vn.itdvietnam.lichamduong.ui.screens.SettingsScreen
import java.time.LocalDate

private data class NavItem(val dest: Dest, val label: String, val icon: ImageVector)

private val compactItems = listOf(
    NavItem(Dest.MONTH, "Tháng", Icons.Outlined.CalendarMonth),
    NavItem(Dest.DAY, "Ngày", Icons.Outlined.Today),
    NavItem(Dest.CONVERT, "Đổi ngày", Icons.Outlined.SyncAlt),
    NavItem(Dest.REMINDERS, "Nhắc lịch", Icons.Outlined.Notifications),
)

private val wideItems = listOf(
    NavItem(Dest.MONTH, "Lịch", Icons.Outlined.CalendarMonth),
    NavItem(Dest.CONVERT, "Đổi ngày", Icons.Outlined.SyncAlt),
    NavItem(Dest.REMINDERS, "Nhắc lịch", Icons.Outlined.Notifications),
    NavItem(Dest.SETTINGS, "Cài đặt", Icons.Outlined.Settings),
)

/**
 * Khung ứng dụng co giãn theo độ rộng màn hình:
 * - Hẹp (điện thoại thanh, màn hình ngoài Z Fold): một ngăn + thanh điều hướng dưới.
 * - Trung bình (Z Fold mở dọc): hai ngăn chia đôi đúng nếp gập + thanh điều hướng dưới.
 * - Rộng (máy tính bảng, Z Fold mở ngang): thanh điều hướng dọc + hai ngăn.
 */
@Composable
fun LichAppUi(vm: AppViewModel, widthSizeClass: WindowWidthSizeClass) {
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()
    val textSize by vm.textSize.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var notificationsAllowed by remember { mutableStateOf(ReminderNotifications.canNotify(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsAllowed = it
    }
    val requestNotifications: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsAllowed) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    // Sao lưu & chuyển máy: người dùng tự chọn nơi lưu (Tải xuống, Google Drive…) và file để nhập
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(vm.exportBackup().toByteArray(Charsets.UTF_8)) }
                    ?: error("Không mở được file")
            }.onSuccess { toast("Đã xuất nhắc lịch và việc lặp ra file sao lưu") }
                .onFailure { toast("Không lưu được file sao lưu") }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: error("Không đọc được file")
                vm.importBackup(text)
            }.onSuccess { r ->
                toast("Đã nhập ${r.added} mục" + if (r.skipped > 0) ", bỏ qua ${r.skipped} mục trùng" else "")
                if (r.added > 0) requestNotifications()
            }.onFailure { toast("File không đúng định dạng sao lưu của Lịch Âm Dương") }
        }
    }

    val actions = AppActions(
        vm = vm,
        save = { vm.save(it); requestNotifications() },
        saveRoutine = { vm.saveRoutine(it); requestNotifications() },
        requestNotifications = requestNotifications,
        exportBackup = { exportLauncher.launch("lich-am-duong-sao-luu-${LocalDate.now()}.json") },
        importBackup = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream", "*/*")) },
    )

    BackHandler(enabled = vm.editing != null) { vm.editing = null }
    BackHandler(enabled = vm.editingRoutine != null) { vm.editingRoutine = null }
    BackHandler(enabled = vm.editing == null && vm.editingRoutine == null && vm.dest != Dest.MONTH) { vm.dest = Dest.MONTH }

    val twoPane = widthSizeClass != WindowWidthSizeClass.Compact
    val useRail = widthSizeClass == WindowWidthSizeClass.Expanded

    Scaffold(
        bottomBar = {
            val editingAny = vm.editing != null || vm.editingRoutine != null
            val showNav = !useRail && !(editingAny && !twoPane)
            Column(if (showNav) Modifier else Modifier.navigationBarsPadding()) {
            // Thương hiệu nhà phát triển: luôn hiện ở mọi màn hình
            BrandStrip(onClick = { vm.editing = null; vm.editingRoutine = null; vm.dest = Dest.SETTINGS })
            if (showNav) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    val items = if (twoPane) wideItems else compactItems
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = vm.dest == item.dest || (twoPane && item.dest == Dest.MONTH && vm.dest == Dest.DAY),
                            onClick = { vm.editing = null; vm.editingRoutine = null; vm.dest = item.dest },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (twoPane) {
                TwoPaneLayout(vm, reminders, themeMode, textSize, notificationsAllowed, actions, useRail)
            } else {
                CompactLayout(vm, reminders, themeMode, textSize, notificationsAllowed, actions)
            }
        }
    }
}

private class AppActions(
    val vm: AppViewModel,
    val save: (Reminder) -> Unit,
    val saveRoutine: (Routine) -> Unit,
    val requestNotifications: () -> Unit,
    val exportBackup: () -> Unit,
    val importBackup: () -> Unit,
) {
    fun newReminderFor(date: LocalDate) {
        val l = DayInfo.of(date).lunar
        vm.newReminder(CalendarType.LUNAR, l.day, l.month, l.year, RepeatType.YEARLY)
    }
}

@Composable
private fun TitleBar(title: String, onBack: (() -> Unit)? = null, onSettings: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = if (onBack == null) 20.dp else 4.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại") }
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (onSettings != null) {
            IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, contentDescription = "Cài đặt và giới thiệu") }
        }
    }
}

@Composable
private fun CompactLayout(
    vm: AppViewModel,
    reminders: List<Reminder>,
    themeMode: vn.itdvietnam.lichamduong.settings.ThemeMode,
    textSize: vn.itdvietnam.lichamduong.settings.TextSize,
    notificationsAllowed: Boolean,
    actions: AppActions,
) {
    val editing = vm.editing
    if (editing != null) {
        ReminderEditScreen(
            initial = editing,
            onSave = actions.save,
            onDelete = vm::delete,
            onClose = { vm.editing = null },
            modifier = Modifier.fillMaxSize(),
        )
        return
    }
    val editingRoutine = vm.editingRoutine
    if (editingRoutine != null) {
        RoutineEditScreen(
            initial = editingRoutine,
            onSave = actions.saveRoutine,
            onDelete = vm::deleteRoutine,
            onClose = { vm.editingRoutine = null },
            modifier = Modifier.fillMaxSize(),
        )
        return
    }
    val openSettings = { vm.dest = Dest.SETTINGS }
    when (vm.dest) {
        Dest.MONTH -> MonthPane(vm, showEvents = false, showSelectedCard = true, onSettings = openSettings)
        Dest.DAY -> DayDetail(
            info = DayInfo.of(vm.selectedDate),
            onPrev = { vm.selectDate(vm.selectedDate.minusDays(1)) },
            onNext = { vm.selectDate(vm.selectedDate.plusDays(1)) },
            onToday = vm::goToday,
            onAddReminder = { actions.newReminderFor(vm.selectedDate) },
            modifier = Modifier.fillMaxSize(),
        )
        Dest.CONVERT -> Column(Modifier.fillMaxSize()) {
            TitleBar("Đổi ngày", onSettings = openSettings)
            ConvertScreen(
                onCreateReminder = { l -> vm.newReminder(CalendarType.LUNAR, l.day, l.month, l.year, RepeatType.YEARLY) },
                modifier = Modifier.fillMaxSize(),
            )
        }
        Dest.REMINDERS -> RemindersHub(vm, reminders, onSettings = openSettings)
        Dest.SETTINGS -> Column(Modifier.fillMaxSize()) {
            TitleBar("Cài đặt", onBack = { vm.dest = Dest.MONTH })
            SettingsScreen(
                themeMode = themeMode,
                textSize = textSize,
                notificationsAllowed = notificationsAllowed,
                onThemeMode = vm::setThemeMode,
                onTextSize = vm::setTextSize,
                onRequestNotifications = actions.requestNotifications,
                reminderCount = reminders.size + vm.routines.value.size,
                onExport = actions.exportBackup,
                onImport = actions.importBackup,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun MonthPane(vm: AppViewModel, showEvents: Boolean, showSelectedCard: Boolean, onSettings: (() -> Unit)?) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MonthHeader(
            month = vm.visibleMonth,
            onPrev = { vm.showMonth(vm.visibleMonth.minusMonths(1)) },
            onNext = { vm.showMonth(vm.visibleMonth.plusMonths(1)) },
            onToday = vm::goToday,
            onSettings = onSettings,
            modifier = Modifier.padding(start = 8.dp),
        )
        MonthGrid(
            month = vm.visibleMonth,
            selected = vm.selectedDate,
            today = LocalDate.now(),
            onSelect = { vm.selectDate(it) },
            onPrevMonth = { vm.showMonth(vm.visibleMonth.minusMonths(1)) },
            onNextMonth = { vm.showMonth(vm.visibleMonth.plusMonths(1)) },
        )
        if (showSelectedCard) {
            SelectedDayCard(info = DayInfo.of(vm.selectedDate), onOpenDay = { vm.dest = Dest.DAY })
        }
        if (showEvents) {
            MonthEventList(month = vm.visibleMonth, selected = vm.selectedDate, onSelect = { vm.selectDate(it) })
        }
    }
}

@Composable
private fun TwoPaneLayout(
    vm: AppViewModel,
    reminders: List<Reminder>,
    themeMode: vn.itdvietnam.lichamduong.settings.ThemeMode,
    textSize: vn.itdvietnam.lichamduong.settings.TextSize,
    notificationsAllowed: Boolean,
    actions: AppActions,
    useRail: Boolean,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val railWidth = if (useRail) 80.dp else 0.dp
        // Ngăn trái kết thúc đúng giữa màn hình → đường chia trùng nếp gập của Z Fold
        val leftWidth = (maxWidth / 2 - railWidth).coerceAtLeast(300.dp)
        Row(Modifier.fillMaxSize()) {
            if (useRail) {
                NavigationRail(
                    modifier = Modifier.width(railWidth).fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                ) {
                    wideItems.forEach { item ->
                        NavigationRailItem(
                            selected = vm.dest == item.dest || (item.dest == Dest.MONTH && vm.dest == Dest.DAY),
                            onClick = { vm.editing = null; vm.editingRoutine = null; vm.dest = item.dest },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
            if (vm.dest == Dest.SETTINGS) {
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                    Column(Modifier.widthIn(max = 640.dp)) {
                        TitleBar("Cài đặt")
                        SettingsScreen(
                            themeMode = themeMode,
                            textSize = textSize,
                            notificationsAllowed = notificationsAllowed,
                            onThemeMode = vm::setThemeMode,
                            onTextSize = vm::setTextSize,
                            onRequestNotifications = actions.requestNotifications,
                reminderCount = reminders.size + vm.routines.value.size,
                onExport = actions.exportBackup,
                onImport = actions.importBackup,
                        )
                    }
                }
            } else {
                Box(Modifier.width(leftWidth).fillMaxHeight()) { LeftPane(vm, reminders) }
                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Box(Modifier.weight(1f).fillMaxHeight()) { RightPane(vm, reminders, actions) }
            }
        }
    }
}

@Composable
private fun LeftPane(vm: AppViewModel, reminders: List<Reminder>) {
    when (vm.dest) {
        Dest.CONVERT -> Column(Modifier.fillMaxSize()) {
            TitleBar("Đổi ngày")
            ConvertScreen(
                onCreateReminder = { l -> vm.newReminder(CalendarType.LUNAR, l.day, l.month, l.year, RepeatType.YEARLY) },
                modifier = Modifier.fillMaxSize(),
            )
        }
        Dest.REMINDERS -> RemindersHub(vm, reminders, onSettings = null)
        else -> MonthPane(vm, showEvents = true, showSelectedCard = false, onSettings = null)
    }
}

@Composable
private fun RightPane(vm: AppViewModel, reminders: List<Reminder>, actions: AppActions) {
    when (vm.dest) {
        Dest.CONVERT -> Column(Modifier.fillMaxSize()) {
            TitleBar("Nhắc lịch")
            RemindersScreen(
                reminders = reminders,
                selectedId = null,
                onOpen = { vm.dest = Dest.REMINDERS; vm.edit(it) },
                onToggle = vm::setEnabled,
                onCreate = { vm.newReminder() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        Dest.REMINDERS -> {
            val editing = vm.editing
            val editingRoutine = vm.editingRoutine
            if (editingRoutine != null) {
                RoutineEditScreen(
                    initial = editingRoutine,
                    onSave = actions.saveRoutine,
                    onDelete = vm::deleteRoutine,
                    onClose = { vm.editingRoutine = null },
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (editing != null) {
                ReminderEditScreen(
                    initial = editing,
                    onSave = actions.save,
                    onDelete = vm::delete,
                    onClose = { vm.editing = null },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val routineTab = vm.remindersTab == 1
                    Text(
                        if (routineTab) "Chọn một việc lặp bên trái để sửa, hoặc thêm việc mới."
                        else "Chọn một nhắc lịch bên trái để sửa, hoặc tạo nhắc mới.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = { if (routineTab) vm.newRoutine() else vm.newReminder() },
                        modifier = Modifier.padding(top = 16.dp),
                    ) { Text(if (routineTab) "Thêm việc lặp" else "Tạo nhắc lịch") }
                }
            }
        }
        else -> DayDetail(
            info = DayInfo.of(vm.selectedDate),
            onPrev = { vm.selectDate(vm.selectedDate.minusDays(1)) },
            onNext = { vm.selectDate(vm.selectedDate.plusDays(1)) },
            onToday = vm::goToday,
            onAddReminder = { actions.newReminderFor(vm.selectedDate) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** Màn hình Nhắc lịch: hai thẻ "Theo ngày âm / dương" và "Lặp hằng ngày". */
@Composable
private fun RemindersHub(vm: AppViewModel, reminders: List<Reminder>, onSettings: (() -> Unit)?) {
    val routines by vm.routines.collectAsStateWithLifecycle()
    val done by vm.routineDone.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        TitleBar("Nhắc lịch", onSettings = onSettings)
        SegmentedChoice(
            options = listOf("Theo ngày âm / dương", "Lặp hằng ngày"),
            selectedIndex = vm.remindersTab,
            onSelect = { vm.remindersTab = it; vm.editing = null; vm.editingRoutine = null },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        if (vm.remindersTab == 0) {
            RemindersScreen(
                reminders = reminders,
                selectedId = vm.editing?.id,
                onOpen = vm::edit,
                onToggle = vm::setEnabled,
                onCreate = { vm.newReminder() },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            RoutinesScreen(
                routines = routines,
                done = done,
                selectedId = vm.editingRoutine?.id,
                onToggleDone = vm::toggleRoutineDone,
                onOpen = vm::editRoutine,
                onToggle = vm::setRoutineEnabled,
                onCreate = { template -> if (template == null) vm.newRoutine() else { vm.newRoutine(); vm.editingRoutine = template } },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
