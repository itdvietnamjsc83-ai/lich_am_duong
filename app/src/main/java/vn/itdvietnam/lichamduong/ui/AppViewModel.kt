package vn.itdvietnam.lichamduong.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import vn.itdvietnam.lichamduong.reminder.CalendarType
import vn.itdvietnam.lichamduong.routine.Routine
import vn.itdvietnam.lichamduong.reminder.Reminder
import vn.itdvietnam.lichamduong.reminder.ReminderRepository
import vn.itdvietnam.lichamduong.reminder.RepeatType
import vn.itdvietnam.lichamduong.settings.SettingsRepository
import vn.itdvietnam.lichamduong.settings.TextSize
import vn.itdvietnam.lichamduong.settings.ThemeMode
import java.time.LocalDate
import java.time.YearMonth

enum class Dest { MONTH, DAY, CONVERT, REMINDERS, SETTINGS }

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val reminderRepo = ReminderRepository.get(app)
    private val routineRepo = vn.itdvietnam.lichamduong.routine.RoutineRepository.get(app)
    private val settingsRepo = SettingsRepository.get(app)

    val reminders = reminderRepo.reminders
    val themeMode = settingsRepo.themeMode
    val textSize = settingsRepo.textSize

    var dest by mutableStateOf(Dest.MONTH)
    var selectedDate: LocalDate by mutableStateOf(LocalDate.now())
        private set
    var visibleMonth: YearMonth by mutableStateOf(YearMonth.now())
        private set

    /** Nhắc lịch đang mở để sửa (id = 0 là tạo mới); null khi không mở trình soạn. */
    var editing: Reminder? by mutableStateOf(null)

    fun selectDate(date: LocalDate) {
        selectedDate = date
        visibleMonth = YearMonth.from(date)
    }

    fun goToday() = selectDate(LocalDate.now())

    fun showMonth(month: YearMonth) {
        visibleMonth = month
    }

    fun newReminder(
        calendar: CalendarType = CalendarType.LUNAR,
        day: Int = 1,
        month: Int = 1,
        year: Int = LocalDate.now().year,
        repeat: RepeatType = RepeatType.YEARLY,
    ) {
        editing = Reminder(
            id = 0L, title = "", calendar = calendar, repeat = repeat,
            day = day, month = month, year = year, beforeDays = 1, hour = 7,
        )
        editingRoutine = null
        remindersTab = 0
        dest = Dest.REMINDERS
    }

    fun edit(reminder: Reminder) {
        editing = reminder
        editingRoutine = null
    }

    fun save(reminder: Reminder) {
        val title = reminder.title.trim().ifEmpty { "Nhắc lịch" }
        reminderRepo.upsert(reminder.copy(title = title))
        editing = null
    }

    fun delete(reminder: Reminder) {
        if (reminder.id != 0L) reminderRepo.delete(reminder.id)
        editing = null
    }

    fun setEnabled(reminder: Reminder, enabled: Boolean) = reminderRepo.setEnabled(reminder.id, enabled)

    /** Sao lưu chung: nhắc lịch theo ngày + việc lặp hằng ngày trong một file. */
    fun exportBackup(): String =
        org.json.JSONObject(reminderRepo.exportJson()).put("routines", routineRepo.exportArray()).toString(2)

    fun importBackup(text: String): ReminderRepository.ImportResult {
        val root = org.json.JSONObject(text)
        val r = if (root.has("reminders")) reminderRepo.importJson(text) else {
            require(root.optString("app") == "lich-am-duong") { "Sai định dạng" }
            ReminderRepository.ImportResult(0, 0)
        }
        val (a, s) = root.optJSONArray("routines")?.let { routineRepo.importArray(it) } ?: (0 to 0)
        return ReminderRepository.ImportResult(r.added + a, r.skipped + s)
    }

    // ---------- Việc lặp hằng ngày ----------
    val routines = routineRepo.routines
    val routineDone = routineRepo.done

    /** 0 = nhắc theo ngày âm/dương, 1 = lặp hằng ngày */
    var remindersTab by mutableStateOf(0)

    /** Việc lặp đang mở để sửa (id = 0 là tạo mới). */
    var editingRoutine: Routine? by mutableStateOf(null)

    fun newRoutine() {
        editingRoutine = Routine(id = 0L, title = "", times = emptyList(), startDate = LocalDate.now())
        editing = null
        remindersTab = 1
        dest = Dest.REMINDERS
    }

    fun editRoutine(r: Routine) {
        editingRoutine = r
        editing = null
    }

    fun saveRoutine(r: Routine) {
        routineRepo.upsert(r.copy(title = r.title.trim().ifEmpty { "Việc hằng ngày" }, who = r.who.trim(), note = r.note.trim()))
        editingRoutine = null
    }

    fun deleteRoutine(r: Routine) {
        if (r.id != 0L) routineRepo.delete(r.id)
        editingRoutine = null
    }

    fun setRoutineEnabled(r: Routine, on: Boolean) = routineRepo.setEnabled(r.id, on)
    fun toggleRoutineDone(key: String) = routineRepo.toggleDone(key)

    fun setThemeMode(mode: ThemeMode) = settingsRepo.setThemeMode(mode)
    fun setTextSize(size: TextSize) = settingsRepo.setTextSize(size)
}
