package vn.itdvietnam.lichamduong

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import vn.itdvietnam.lichamduong.lunar.CanChi
import vn.itdvietnam.lichamduong.lunar.DayInfo
import vn.itdvietnam.lichamduong.lunar.LunarCalendar
import vn.itdvietnam.lichamduong.lunar.LunarDate
import vn.itdvietnam.lichamduong.reminder.CalendarType
import vn.itdvietnam.lichamduong.reminder.Recurrence
import vn.itdvietnam.lichamduong.reminder.Reminder
import vn.itdvietnam.lichamduong.reminder.RepeatType
import java.time.LocalDate
import java.time.LocalDateTime

class LunarCalendarTest {

    @Test
    fun solarToLunar_knownDates() {
        assertEquals(LunarDate(1, 1, 2026), LunarCalendar.solarToLunar(LocalDate.of(2026, 2, 17))) // Tết Bính Ngọ
        assertEquals(LunarDate(1, 1, 2025), LunarCalendar.solarToLunar(LocalDate.of(2025, 1, 29))) // Tết Ất Tỵ
        assertEquals(LunarDate(1, 1, 2024), LunarCalendar.solarToLunar(LocalDate.of(2024, 2, 10))) // Tết Giáp Thìn
        assertEquals(LunarDate(1, 9, 2026), LunarCalendar.solarToLunar(LocalDate.of(2026, 10, 10)))
        assertEquals(LunarDate(1, 6, 2025, leap = true), LunarCalendar.solarToLunar(LocalDate.of(2025, 7, 25)))
        assertEquals(LunarDate(29, 6, 2025), LunarCalendar.solarToLunar(LocalDate.of(2025, 7, 23)))
    }

    @Test
    fun lunarToSolar_roundTrip() {
        assertEquals(LocalDate.of(2027, 4, 18), LunarCalendar.lunarToSolar(12, 3, 2027))
        assertEquals(LocalDate.of(2025, 7, 25), LunarCalendar.lunarToSolar(1, 6, 2025, leap = true))
        assertNull(LunarCalendar.lunarToSolar(1, 6, 2026, leap = true)) // 2026 không nhuận
        assertNull(LunarCalendar.lunarToSolar(30, 8, 2026)) // tháng 8/2026 thiếu
        var d = LocalDate.of(2000, 1, 1)
        while (d.year < 2060) {
            val l = LunarCalendar.solarToLunar(d)
            assertEquals(d, LunarCalendar.lunarToSolar(l.day, l.month, l.year, l.leap))
            d = d.plusDays(13)
        }
    }

    @Test
    fun leapMonths() {
        assertEquals(6, LunarCalendar.leapMonthOf(2025))
        assertEquals(0, LunarCalendar.leapMonthOf(2026))
        assertEquals(5, LunarCalendar.leapMonthOf(2028))
        assertEquals(4, LunarCalendar.leapMonthOf(2020))
    }

    @Test
    fun canChiAndTietKhi() {
        val info = DayInfo.of(LocalDate.of(2026, 10, 10))
        assertEquals("Đinh Tỵ", info.dayCanChi)
        assertEquals("Mậu Tuất", info.monthCanChi)
        assertEquals("Bính Ngọ", info.yearCanChi)
        assertEquals("Thứ Bảy", info.weekday)
        assertEquals(listOf("Sửu", "Thìn", "Ngọ", "Mùi", "Tuất", "Hợi"), info.gioHoangDao.map { it.chi })
        assertEquals("Thu phân", CanChi.tietKhi(LunarCalendar.jdFromDate(LocalDate.of(2026, 10, 7))))
        assertEquals("Hàn lộ", CanChi.tietKhi(LunarCalendar.jdFromDate(LocalDate.of(2026, 10, 8))))
        assertEquals("Sương giáng", CanChi.tietKhi(LunarCalendar.jdFromDate(LocalDate.of(2026, 10, 23))))
        assertEquals("Giáp Thìn", CanChi.year(2024))
    }

    @Test
    fun recurrence_lunarAndSolar() {
        val today = LocalDate.of(2026, 10, 10)
        val gio = Reminder(1, "Giỗ", CalendarType.LUNAR, RepeatType.YEARLY, day = 8, month = 11, year = 2026)
        assertEquals(
            listOf(LocalDate.of(2026, 12, 16), LocalDate.of(2027, 12, 5)),
            Recurrence.nextOccurrences(gio, today, 2),
        )
        val ram = Reminder(2, "Mùng 1 & Rằm", CalendarType.LUNAR, RepeatType.MONTHLY, 1, 0, 0, extraDays = listOf(15))
        assertEquals(
            listOf(LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 24), LocalDate.of(2026, 11, 9)),
            Recurrence.nextOccurrences(ram, today, 3),
        )
        // Ngày 30 của tháng thiếu → nhắc ngày 29
        val d30 = Reminder(3, "Cuối tháng", CalendarType.LUNAR, RepeatType.MONTHLY, 30, 0, 0)
        assertEquals(29, LunarCalendar.monthLength(7, 2026))
        assertEquals(LunarDate(29, 7, 2026), LunarCalendar.solarToLunar(Recurrence.nextOccurrences(d30, LocalDate.of(2026, 9, 1), 1)[0]))
        assertEquals(30, LunarCalendar.monthLength(9, 2026))
        assertEquals(LunarDate(30, 9, 2026), LunarCalendar.solarToLunar(Recurrence.nextOccurrences(d30, LocalDate.of(2026, 10, 10), 2)[0]))
        val cuoi = Reminder(4, "Kỷ niệm", CalendarType.SOLAR, RepeatType.YEARLY, 5, 1, 0)
        assertEquals(LocalDate.of(2027, 1, 5), Recurrence.nextOccurrences(cuoi, today, 1)[0])
        val alarm = Recurrence.nextAlarm(gio.copy(beforeDays = 3, hour = 7), LocalDateTime.of(2026, 10, 10, 9, 0))
        assertEquals(LocalDateTime.of(2026, 12, 13, 7, 0), alarm?.second)
        // Giờ đổ chuông riêng, chọn tự do: 5:37 sáng, trước 1 ngày; thông báo 19:45 trước 3 ngày
        val ring = gio.copy(beforeDays = 3, hour = 19, minute = 45, alarm = true, alarmBeforeDays = 1, alarmHour = 5, alarmMinute = 37)
        assertEquals(LocalDateTime.of(2026, 12, 15, 5, 37), Recurrence.nextRing(ring, LocalDateTime.of(2026, 10, 10, 9, 0))?.second)
        assertEquals(LocalDateTime.of(2026, 12, 13, 19, 45), Recurrence.nextAlarm(ring, LocalDateTime.of(2026, 10, 10, 9, 0))?.second)
        assertEquals(null, Recurrence.nextRing(gio, LocalDateTime.of(2026, 10, 10, 9, 0)))
        assertEquals(true, ring.copy(alarmBeforeDays = 3, alarmHour = 19, alarmMinute = 45).ringReplacesNotification)
    }
}
