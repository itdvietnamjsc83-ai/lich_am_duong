package vn.itdvietnam.lichamduong.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import vn.itdvietnam.lichamduong.reminder.CalendarType
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
        dest = Dest.REMINDERS
    }

    fun edit(reminder: Reminder) {
        editing = reminder
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

    fun setThemeMode(mode: ThemeMode) = settingsRepo.setThemeMode(mode)
    fun setTextSize(size: TextSize) = settingsRepo.setTextSize(size)
}
