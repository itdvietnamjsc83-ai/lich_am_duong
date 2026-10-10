package vn.itdvietnam.lichamduong.reminder

import vn.itdvietnam.lichamduong.lunar.LunarCalendar
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Tính các lần nhắc kế tiếp theo lịch âm hoặc dương.
 *
 * Quy tắc riêng cho lịch âm:
 * - Chọn ngày 30 mà tháng âm chỉ có 29 ngày → nhắc vào ngày 29.
 * - Lặp hằng năm: nhắc ở tháng chính, không nhắc lại ở tháng nhuận trùng tên.
 * - Lặp hằng tháng: nhắc cả trong tháng nhuận (tháng nhuận cũng có mùng 1, rằm).
 * Với lịch dương, ngày không tồn tại (ví dụ 31/4, 29/2 năm thường) dời về ngày cuối tháng.
 */
object Recurrence {

    fun nextOccurrences(r: Reminder, from: LocalDate, count: Int): List<LocalDate> =
        if (r.isLunar) lunar(r, from, count) else solar(r, from, count)

    /** Lần báo kế tiếp sau thời điểm [now]: (ngày nhắc, thời điểm báo). */
    fun nextAlarm(r: Reminder, now: LocalDateTime): Pair<LocalDate, LocalDateTime>? {
        if (!r.enabled) return null
        return nextOccurrences(r, now.toLocalDate(), 8)
            .map { it to it.minusDays(r.beforeDays.toLong()).atTime(r.hour, r.minute) }
            .firstOrNull { it.second.isAfter(now) }
    }

    private fun solar(r: Reminder, from: LocalDate, count: Int): List<LocalDate> {
        val out = mutableListOf<LocalDate>()
        when (r.repeat) {
            RepeatType.ONCE -> {
                val d = solarDate(r.year, r.month, r.day)
                if (!d.isBefore(from)) out.add(d)
            }
            RepeatType.YEARLY -> {
                var y = from.year
                while (out.size < count && y < from.year + count + 2) {
                    val d = solarDate(y, r.month, r.day)
                    if (!d.isBefore(from)) out.add(d)
                    y++
                }
            }
            RepeatType.MONTHLY -> {
                var ym = YearMonth.from(from)
                var guard = 0
                while (out.size < count && guard < count + 2) {
                    for (day in r.daysOfMonth) {
                        val d = ym.atDay(minOf(day, ym.lengthOfMonth()))
                        if (!d.isBefore(from) && out.size < count && d !in out) out.add(d)
                    }
                    ym = ym.plusMonths(1)
                    guard++
                }
            }
        }
        return out
    }

    private fun solarDate(year: Int, month: Int, day: Int): LocalDate {
        val ym = YearMonth.of(year, month)
        return ym.atDay(minOf(day, ym.lengthOfMonth()))
    }

    private fun lunar(r: Reminder, from: LocalDate, count: Int): List<LocalDate> {
        val fromJd = LunarCalendar.jdFromDate(from)
        val out = mutableListOf<Int>()
        when (r.repeat) {
            RepeatType.MONTHLY -> {
                var monthStart = LunarCalendar.solarToLunarJd(fromJd).monthStartJd
                var guard = 0
                while (out.size < count && guard < count + 2) {
                    val len = LunarCalendar.monthLength(monthStart)
                    for (day in r.daysOfMonth) {
                        val jd = monthStart + minOf(day, len) - 1
                        if (jd >= fromJd && out.size < count && jd !in out) out.add(jd)
                    }
                    monthStart += len
                    guard++
                }
            }
            RepeatType.YEARLY -> {
                val startYear = LunarCalendar.solarToLunarJd(fromJd).date.year
                var y = startYear
                while (out.size < count && y < startYear + count + 2) {
                    val start = LunarCalendar.lunarToSolarJd(1, r.month, y, false)
                    if (start != null) {
                        val jd = start + minOf(r.day, LunarCalendar.monthLength(start)) - 1
                        if (jd >= fromJd) out.add(jd)
                    }
                    y++
                }
            }
            RepeatType.ONCE -> {
                val start = LunarCalendar.lunarToSolarJd(1, r.month, r.year, false)
                if (start != null) {
                    val jd = start + minOf(r.day, LunarCalendar.monthLength(start)) - 1
                    if (jd >= fromJd) out.add(jd)
                }
            }
        }
        return out.map { LunarCalendar.jdToDate(it) }
    }
}
