package vn.itdvietnam.lichamduong.reminder

import vn.itdvietnam.lichamduong.lunar.CanChi

enum class CalendarType { LUNAR, SOLAR }

enum class RepeatType(val label: String) {
    ONCE("Một lần"),
    MONTHLY("Hằng tháng"),
    YEARLY("Hằng năm"),
}

/**
 * Một nhắc lịch.
 * - [day]/[month]/[year] là ngày theo lịch [calendar] (âm hoặc dương).
 * - [month] bỏ qua khi lặp hằng tháng; [year] chỉ dùng khi nhắc một lần.
 * - [extraDays]: các ngày khác trong tháng khi lặp hằng tháng (ví dụ mùng 1 và rằm).
 * - Báo lúc [hour]:[minute], trước ngày nhắc [beforeDays] ngày.
 */
data class Reminder(
    val id: Long,
    val title: String,
    val calendar: CalendarType,
    val repeat: RepeatType,
    val day: Int,
    val month: Int,
    val year: Int,
    val extraDays: List<Int> = emptyList(),
    val beforeDays: Int = 0,
    val hour: Int = 7,
    val minute: Int = 0,
    val enabled: Boolean = true,
    /** Đổ chuông và rung liên tục như báo thức (thay vì chỉ hiện thông báo). */
    val alarm: Boolean = false,
    /** Giờ đổ chuông riêng: trước ngày nhắc [alarmBeforeDays] ngày, lúc [alarmHour]:[alarmMinute]. */
    val alarmBeforeDays: Int = 0,
    val alarmHour: Int = 7,
    val alarmMinute: Int = 0,
    /** Báo lại sau bao nhiêu phút khi bấm "Báo lại" trên màn hình chuông. */
    val snoozeMinutes: Int = 10,
) {
    /** Chuông trùng đúng giờ thông báo thì chỉ đổ chuông, không hiện thêm thông báo. */
    val ringReplacesNotification: Boolean
        get() = alarm && alarmBeforeDays == beforeDays && alarmHour == hour && alarmMinute == minute

    val isLunar: Boolean get() = calendar == CalendarType.LUNAR

    /** Các ngày trong tháng cần nhắc, đã sắp xếp. */
    val daysOfMonth: List<Int> get() = (listOf(day) + extraDays).distinct().sorted()

    fun ruleText(): String {
        val cal = if (isLunar) "âm lịch" else "dương lịch"
        return when (repeat) {
            RepeatType.MONTHLY -> {
                val days = daysOfMonth.joinToString(" và ") { if (isLunar) CanChi.lunarDayName(it).lowercase() else "ngày $it" }
                "${days.replaceFirstChar { it.uppercase() }} $cal · Hằng tháng"
            }
            RepeatType.YEARLY ->
                if (isLunar) "Ngày $day tháng ${CanChi.MONTH_NAMES[month]} $cal · Hằng năm"
                else "Ngày $day/$month $cal · Hằng năm"
            RepeatType.ONCE ->
                if (isLunar) "Ngày $day tháng ${CanChi.MONTH_NAMES[month]} năm $year $cal"
                else "Ngày $day/$month/$year"
        }
    }

    fun alertText(): String {
        fun at(before: Int, h: Int, m: Int) = "%02d:%02d".format(h, m) + if (before == 0) " đúng ngày" else ", trước $before ngày"
        if (ringReplacesNotification) return "Đổ chuông " + at(alarmBeforeDays, alarmHour, alarmMinute)
        val notify = "Thông báo " + at(beforeDays, hour, minute)
        return if (alarm) "$notify · Chuông " + at(alarmBeforeDays, alarmHour, alarmMinute) else notify
    }
}
