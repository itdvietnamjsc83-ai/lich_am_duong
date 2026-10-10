package vn.itdvietnam.lichamduong.lunar

import java.time.LocalDate

/** Mọi thông tin hiển thị của một ngày. */
data class DayInfo(
    val date: LocalDate,
    val jd: Int,
    val lunar: LunarDate,
    val lunarMonthLength: Int,
    val weekday: String,
    val dayCanChi: String,
    val monthCanChi: String,
    val yearCanChi: String,
    val tietKhi: String,
    val moonPhase: String,
    val events: List<String>,
    val gioHoangDao: List<CanChi.Gio>,
) {
    /** Mùng 1 hoặc Rằm: tô màu nhấn trên lịch. */
    val isSpecialLunarDay: Boolean get() = lunar.day == 1 || lunar.day == 15
    val solarText: String get() = "${date.dayOfMonth}/${date.monthValue}/${date.year}"

    companion object {
        fun of(date: LocalDate): DayInfo {
            val jd = LunarCalendar.jdFromDate(date)
            val info = LunarCalendar.solarToLunarJd(jd)
            val lunar = info.date
            val monthLength = LunarCalendar.monthLength(info.monthStartJd)
            val events = buildList {
                addAll(Holidays.forDate(date, lunar, monthLength))
                CanChi.tietKhiStart(jd)?.let { add("Bắt đầu tiết $it") }
            }
            return DayInfo(
                date = date,
                jd = jd,
                lunar = lunar,
                lunarMonthLength = monthLength,
                weekday = CanChi.WEEKDAYS[date.dayOfWeek.value - 1],
                dayCanChi = CanChi.day(jd),
                monthCanChi = CanChi.month(lunar.month, lunar.year),
                yearCanChi = CanChi.year(lunar.year),
                tietKhi = CanChi.tietKhi(jd),
                moonPhase = CanChi.moonPhase(lunar.day),
                events = events,
                gioHoangDao = CanChi.gioHoangDao(jd),
            )
        }
    }
}
