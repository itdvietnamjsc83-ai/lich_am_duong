package vn.itdvietnam.lichamduong.routine

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Việc lặp hằng ngày: uống thuốc, cho con ăn, đổ nước thải… vào nhiều giờ trong ngày.
 * - [times]: các giờ trong ngày, tính bằng phút từ 0:00 (420 = 07:00), đã sắp xếp, không trùng.
 * - [days]: các thứ áp dụng theo DayOfWeek (1 = Thứ Hai … 7 = Chủ Nhật).
 * - Áp dụng từ [startDate] đến [endDate] (null = không có ngày kết thúc).
 * - [ring] = true: đổ chuông như báo thức; false: chỉ hiện thông báo.
 */
data class Routine(
    val id: Long,
    val title: String,
    val who: String = "",
    val note: String = "",
    val times: List<Int>,
    val days: Set<Int> = ALL_DAYS,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val ring: Boolean = true,
    val snoozeMinutes: Int = 10,
    val enabled: Boolean = true,
) {
    val displayTitle: String get() = if (who.isBlank()) title else "$title · $who"

    fun timesText(): String = times.joinToString(" · ") { RoutineSchedule.format(it) }

    fun daysText(): String = if (days.size == 7) "Hằng ngày" else {
        "Thứ " + days.sorted().joinToString(", ") { if (it == 7) "CN" else "${it + 1}" }
            .replace("Thứ CN", "Chủ Nhật")
    }

    fun ruleText(): String = buildString {
        append(daysText())
        endDate?.let { append(" · đến hết ${it.dayOfMonth}/${it.monthValue}/${it.year}") }
        append(if (ring) " · Đổ chuông" else " · Thông báo")
    }

    companion object {
        val ALL_DAYS: Set<Int> = (1..7).toSet()
    }
}

/** Một lần báo cụ thể trong ngày. */
data class RoutineSlot(val routine: Routine, val date: LocalDate, val minute: Int) {
    val key: String get() = RoutineSchedule.doneKey(routine.id, date, minute)
}

object RoutineSchedule {

    fun format(minute: Int): String = "%02d:%02d".format(minute / 60, minute % 60)

    fun doneKey(id: Long, date: LocalDate, minute: Int): String = "$id|$date|$minute"

    fun occursOn(r: Routine, date: LocalDate): Boolean =
        r.enabled && r.times.isNotEmpty() &&
            !date.isBefore(r.startDate) &&
            (r.endDate == null || !date.isAfter(r.endDate)) &&
            date.dayOfWeek.value in r.days

    /** Lần báo kế tiếp (sau thời điểm [now]) của một việc lặp, hoặc null nếu không còn. */
    fun next(r: Routine, now: LocalDateTime): Pair<LocalDate, Int>? {
        if (!r.enabled || r.times.isEmpty() || r.days.isEmpty()) return null
        var date = if (r.startDate.isAfter(now.toLocalDate())) r.startDate else now.toLocalDate()
        repeat(8) {
            if (r.endDate != null && date.isAfter(r.endDate)) return null
            if (occursOn(r, date)) {
                val nowMinute = now.hour * 60 + now.minute
                val m = r.times.firstOrNull { date.isAfter(now.toLocalDate()) || it > nowMinute }
                if (m != null) return date to m
            }
            date = date.plusDays(1)
        }
        return null
    }

    /** Toàn bộ các lần báo trong ngày [date], sắp theo giờ. */
    fun slotsOn(list: List<Routine>, date: LocalDate): List<RoutineSlot> =
        list.filter { occursOn(it, date) }
            .flatMap { r -> r.times.map { RoutineSlot(r, date, it) } }
            .sortedWith(compareBy({ it.minute }, { it.routine.title }))

    /** Số lần báo của việc lặp trong khoảng áp dụng (null nếu không có ngày kết thúc). */
    fun totalCount(r: Routine): Int? {
        val end = r.endDate ?: return null
        var n = 0
        var d = r.startDate
        while (!d.isAfter(end)) {
            if (d.dayOfWeek.value in r.days) n += r.times.size
            d = d.plusDays(1)
        }
        return n
    }

    /**
     * Đọc giờ gõ nhanh: "7h 11h30 19h", "7:00, 19:30", "7g 12g", "7 12 19".
     * Trả về phút trong ngày, bỏ giá trị sai.
     */
    fun parseTimes(text: String): List<Int> {
        val re = Regex("""(\d{1,2})\s*(?:[:hHgG.](\d{1,2})?)?""")
        return re.findAll(text).mapNotNull { m ->
            val h = m.groupValues[1].toInt()
            val mi = m.groupValues[2].ifEmpty { "0" }.toInt()
            if (h < 24 && mi < 60) h * 60 + mi else null
        }.toList()
    }

    /** Tạo giờ theo chu kỳ: mỗi [everyMinutes] phút từ [from] đến [to] (phút trong ngày). */
    fun cycle(everyMinutes: Int, from: Int, to: Int): List<Int> {
        if (everyMinutes <= 0 || from > to) return emptyList()
        return generateSequence(from) { it + everyMinutes }.takeWhile { it <= to && it < 24 * 60 }.toList()
    }

    fun normalize(times: Collection<Int>): List<Int> = times.filter { it in 0 until 24 * 60 }.distinct().sorted()
}
