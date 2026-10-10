package vn.itdvietnam.lichamduong.lunar

import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.sin

/** Một ngày âm lịch. [leap] = true khi thuộc tháng nhuận. */
data class LunarDate(val day: Int, val month: Int, val year: Int, val leap: Boolean = false)

/** Kết quả đổi dương → âm, kèm số ngày Julius của mùng 1 tháng âm đó. */
data class LunarInfo(val date: LunarDate, val monthStartJd: Int)

/**
 * Lịch âm Việt Nam theo thuật toán thiên văn của Hồ Ngọc Đức, tính theo múi giờ +7.
 * Mọi phép tính chạy trên máy, không cần mạng.
 */
object LunarCalendar {
    const val TIME_ZONE = 7.0

    private fun intFloor(d: Double): Int = floor(d).toInt()

    // ---------- Số ngày Julius ----------

    fun jdFromDate(dd: Int, mm: Int, yy: Int): Int {
        val a = (14 - mm) / 12
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        var jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        if (jd < 2299161) {
            jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
        }
        return jd
    }

    fun jdFromDate(date: LocalDate): Int = jdFromDate(date.dayOfMonth, date.monthValue, date.year)

    fun jdToDate(jd: Int): LocalDate {
        val b: Int
        val c: Int
        if (jd > 2299160) {
            val a = jd + 32044
            b = (4 * a + 3) / 146097
            c = a - (b * 146097) / 4
        } else {
            b = 0
            c = jd + 32082
        }
        val d = (4 * c + 3) / 1461
        val e = c - (1461 * d) / 4
        val m = (5 * e + 2) / 153
        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = b * 100 + d - 4800 + m / 10
        return LocalDate.of(year, month, day)
    }

    // ---------- Thiên văn ----------

    /** Thời điểm sóc (trăng mới) thứ k tính từ 1/1/1900, theo số ngày Julius. */
    private fun newMoon(k: Int): Double {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val dr = PI / 180
        var jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        jd1 += 0.00033 * sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)
        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        val mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
        var c1 = (0.1734 - 0.000393 * t) * sin(m * dr) + 0.0021 * sin(2 * dr * m)
        c1 = c1 - 0.4068 * sin(mpr * dr) + 0.0161 * sin(dr * 2 * mpr)
        c1 -= 0.0004 * sin(dr * 3 * mpr)
        c1 = c1 + 0.0104 * sin(dr * 2 * f) - 0.0051 * sin(dr * (m + mpr))
        c1 = c1 - 0.0074 * sin(dr * (m - mpr)) + 0.0004 * sin(dr * (2 * f + m))
        c1 = c1 - 0.0004 * sin(dr * (2 * f - m)) - 0.0006 * sin(dr * (2 * f + mpr))
        c1 = c1 + 0.0010 * sin(dr * (2 * f - mpr)) + 0.0005 * sin(dr * (2 * mpr + m))
        val deltaT = if (t < -11) {
            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
        } else {
            -0.000278 + 0.000265 * t + 0.000262 * t2
        }
        return jd1 + c1 - deltaT
    }

    private fun newMoonDay(k: Int): Int = intFloor(newMoon(k) + 0.5 + TIME_ZONE / 24)

    /** Kinh độ mặt trời (độ, 0..360) tại thời điểm [jdn] (ngày Julius, nửa đêm giờ địa phương). */
    fun sunLongitudeDeg(jdn: Double): Double {
        val t = (jdn - 2451545.5 - TIME_ZONE / 24) / 36525
        val t2 = t * t
        val dr = PI / 180
        val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        val dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * sin(dr * m) +
            (0.019993 - 0.000101 * t) * sin(dr * 2 * m) + 0.000290 * sin(dr * 3 * m)
        val l = (l0 + dl) % 360.0
        return if (l < 0) l + 360.0 else l
    }

    /** Cung hoàng đạo của mặt trời (0..11), dùng để xác định tháng nhuận. */
    private fun sunLongitudeSector(jdn: Int): Int {
        val t = (jdn - 2451545.5 - TIME_ZONE / 24) / 36525
        val t2 = t * t
        val dr = PI / 180
        val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        val dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * sin(dr * m) +
            (0.019993 - 0.000101 * t) * sin(dr * 2 * m) + 0.000290 * sin(dr * 3 * m)
        var l = (l0 + dl) * dr
        l -= PI * 2 * intFloor(l / (PI * 2))
        return intFloor(l / PI * 6)
    }

    /** Ngày bắt đầu tháng 11 âm lịch (tháng chứa Đông chí) của năm [yy]. */
    private fun lunarMonth11(yy: Int): Int {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = intFloor(off / 29.530588853)
        var nm = newMoonDay(k)
        if (sunLongitudeSector(nm) >= 9) nm = newMoonDay(k - 1)
        return nm
    }

    private fun leapMonthOffset(a11: Int): Int {
        val k = intFloor((a11 - 2415021.076998695) / 29.530588853 + 0.5)
        var last: Int
        var i = 1
        var arc = sunLongitudeSector(newMoonDay(k + i))
        do {
            last = arc
            i++
            arc = sunLongitudeSector(newMoonDay(k + i))
        } while (arc != last && i < 14)
        return i - 1
    }

    // ---------- Đổi lịch ----------

    fun solarToLunarJd(dayNumber: Int): LunarInfo {
        val k = intFloor((dayNumber - 2415021.076998695) / 29.530588853)
        var monthStart = newMoonDay(k + 1)
        if (monthStart > dayNumber) monthStart = newMoonDay(k)
        val yy = jdToDate(dayNumber).year
        var a11 = lunarMonth11(yy)
        var b11 = a11
        var lunarYear: Int
        if (a11 >= monthStart) {
            lunarYear = yy
            a11 = lunarMonth11(yy - 1)
        } else {
            lunarYear = yy + 1
            b11 = lunarMonth11(yy + 1)
        }
        val lunarDay = dayNumber - monthStart + 1
        val diff = intFloor((monthStart - a11) / 29.0)
        var leap = false
        var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapOff = leapMonthOffset(a11)
            if (diff >= leapOff) {
                lunarMonth = diff + 10
                if (diff == leapOff) leap = true
            }
        }
        if (lunarMonth > 12) lunarMonth -= 12
        if (lunarMonth >= 11 && diff < 4) lunarYear -= 1
        return LunarInfo(LunarDate(lunarDay, lunarMonth, lunarYear, leap), monthStart)
    }

    fun solarToLunar(date: LocalDate): LunarDate = solarToLunarJd(jdFromDate(date)).date

    /**
     * Số ngày Julius của ngày âm lịch đã cho, hoặc null nếu tháng nhuận không tồn tại.
     * Không kiểm tra ngày 30 của tháng thiếu: dùng [lunarToSolar] nếu cần kiểm tra.
     */
    fun lunarToSolarJd(day: Int, month: Int, year: Int, leap: Boolean): Int? {
        val a11: Int
        val b11: Int
        if (month < 11) {
            a11 = lunarMonth11(year - 1)
            b11 = lunarMonth11(year)
        } else {
            a11 = lunarMonth11(year)
            b11 = lunarMonth11(year + 1)
        }
        val k = intFloor(0.5 + (a11 - 2415021.076998695) / 29.530588853)
        var off = month - 11
        if (off < 0) off += 12
        if (b11 - a11 > 365) {
            val leapOff = leapMonthOffset(a11)
            var leapMonth = leapOff - 2
            if (leapMonth < 0) leapMonth += 12
            if (leap && month != leapMonth) return null
            if (leap || off >= leapOff) off += 1
        } else if (leap) {
            return null
        }
        return newMoonDay(k + off) + day - 1
    }

    /** Đổi âm → dương; null nếu ngày không tồn tại (tháng nhuận sai hoặc ngày 30 của tháng thiếu). */
    fun lunarToSolar(day: Int, month: Int, year: Int, leap: Boolean = false): LocalDate? {
        if (day !in 1..30 || month !in 1..12) return null
        val start = lunarToSolarJd(1, month, year, leap) ?: return null
        if (day > monthLength(start)) return null
        return jdToDate(start + day - 1)
    }

    /** Độ dài (29 hoặc 30 ngày) của tháng âm bắt đầu tại [monthStartJd]. */
    fun monthLength(monthStartJd: Int): Int =
        if (solarToLunarJd(monthStartJd + 29).date.day == 30) 30 else 29

    fun monthLength(month: Int, year: Int, leap: Boolean = false): Int? =
        lunarToSolarJd(1, month, year, leap)?.let { monthLength(it) }

    /** Tháng nhuận của năm âm [year], hoặc 0 nếu năm không nhuận. */
    fun leapMonthOf(year: Int): Int {
        for (m in 1..12) {
            if (lunarToSolarJd(1, m, year, true) != null) return m
        }
        return 0
    }
}
