package vn.itdvietnam.lichamduong.lunar

import java.time.LocalDate

/** Ngày lễ, ngày kỷ niệm theo dương lịch và âm lịch. */
object Holidays {
    private val solar: Map<Pair<Int, Int>, String> = mapOf(
        (1 to 1) to "Tết Dương lịch",
        (3 to 2) to "Ngày thành lập Đảng Cộng sản Việt Nam",
        (14 to 2) to "Lễ Tình nhân",
        (8 to 3) to "Ngày Quốc tế Phụ nữ",
        (30 to 4) to "Ngày Giải phóng miền Nam",
        (1 to 5) to "Ngày Quốc tế Lao động",
        (19 to 5) to "Ngày sinh Chủ tịch Hồ Chí Minh",
        (1 to 6) to "Ngày Quốc tế Thiếu nhi",
        (27 to 7) to "Ngày Thương binh – Liệt sĩ",
        (2 to 9) to "Quốc khánh",
        (10 to 10) to "Ngày Giải phóng Thủ đô",
        (13 to 10) to "Ngày Doanh nhân Việt Nam",
        (20 to 10) to "Ngày Phụ nữ Việt Nam",
        (20 to 11) to "Ngày Nhà giáo Việt Nam",
        (22 to 12) to "Ngày thành lập Quân đội nhân dân Việt Nam",
        (24 to 12) to "Lễ Giáng sinh",
    )

    private val lunar: Map<Pair<Int, Int>, String> = mapOf(
        (1 to 1) to "Tết Nguyên Đán",
        (2 to 1) to "Mùng 2 Tết",
        (3 to 1) to "Mùng 3 Tết",
        (15 to 1) to "Tết Nguyên Tiêu (Rằm tháng Giêng)",
        (3 to 3) to "Tết Hàn thực",
        (10 to 3) to "Giỗ Tổ Hùng Vương",
        (15 to 4) to "Lễ Phật đản",
        (5 to 5) to "Tết Đoan ngọ",
        (15 to 7) to "Lễ Vu Lan",
        (15 to 8) to "Tết Trung thu",
        (9 to 9) to "Tết Trùng Cửu",
        (23 to 12) to "Ông Công ông Táo",
    )

    fun forDate(date: LocalDate, lunarDate: LunarDate, lunarMonthLength: Int): List<String> = buildList {
        solar[date.dayOfMonth to date.monthValue]?.let { add(it) }
        if (!lunarDate.leap) {
            lunar[lunarDate.day to lunarDate.month]?.let { add(it) }
            if (lunarDate.month == 12 && lunarDate.day == lunarMonthLength) add("Giao thừa")
            if (lunarDate.day == 15 && lunarDate.month !in setOf(1, 4, 7, 8)) {
                add("Rằm tháng " + CanChi.MONTH_NAMES[lunarDate.month])
            }
        }
    }
}
