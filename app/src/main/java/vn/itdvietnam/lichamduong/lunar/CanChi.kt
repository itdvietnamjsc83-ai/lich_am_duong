package vn.itdvietnam.lichamduong.lunar

/** Can chi, giờ hoàng đạo, tiết khí, tên gọi tiếng Việt. */
object CanChi {
    val CAN = listOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    val CHI = listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")

    val MONTH_NAMES = listOf(
        "", "Giêng", "Hai", "Ba", "Tư", "Năm", "Sáu", "Bảy", "Tám", "Chín", "Mười", "Mười Một", "Chạp",
    )

    val WEEKDAYS = listOf("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật")
    val WEEKDAYS_SHORT = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    /** 24 tiết khí, bắt đầu từ Xuân phân (kinh độ mặt trời 0°), mỗi tiết 15°. */
    val TIET_KHI = listOf(
        "Xuân phân", "Thanh minh", "Cốc vũ", "Lập hạ", "Tiểu mãn", "Mang chủng",
        "Hạ chí", "Tiểu thử", "Đại thử", "Lập thu", "Xử thử", "Bạch lộ",
        "Thu phân", "Hàn lộ", "Sương giáng", "Lập đông", "Tiểu tuyết", "Đại tuyết",
        "Đông chí", "Tiểu hàn", "Đại hàn", "Lập xuân", "Vũ thủy", "Kinh trập",
    )

    /** Giờ hoàng đạo theo chi của ngày (chỉ số chi giờ), lặp theo cặp Tý-Ngọ, Sửu-Mùi, ... */
    private val HOANG_DAO = listOf(
        listOf(0, 1, 3, 6, 8, 9),
        listOf(2, 3, 5, 8, 10, 11),
        listOf(0, 1, 4, 5, 7, 10),
        listOf(0, 2, 3, 6, 7, 9),
        listOf(2, 4, 5, 8, 9, 11),
        listOf(1, 4, 6, 7, 10, 11),
    )

    fun year(lunarYear: Int): String = CAN[(lunarYear + 6) % 10] + " " + CHI[(lunarYear + 8) % 12]

    fun month(lunarMonth: Int, lunarYear: Int): String =
        CAN[(lunarYear * 12 + lunarMonth + 3) % 10] + " " + CHI[(lunarMonth + 1) % 12]

    fun day(jd: Int): String = CAN[(jd + 9) % 10] + " " + CHI[(jd + 1) % 12]

    data class Gio(val chi: String, val range: String)

    fun gioHoangDao(jd: Int): List<Gio> {
        val dayChi = (jd + 1) % 12
        return HOANG_DAO[dayChi % 6].map { i ->
            val start = (23 + 2 * i) % 24
            val end = (start + 2) % 24
            Gio(CHI[i], "${start}h – ${end}h")
        }
    }

    /** Chỉ số tiết khí (0..23) đang có hiệu lực vào cuối ngày [jd] theo giờ Việt Nam. */
    fun tietKhiIndex(jd: Int): Int =
        (LunarCalendar.sunLongitudeDeg(jd + 1.0) / 15.0).toInt() % 24

    fun tietKhi(jd: Int): String = TIET_KHI[tietKhiIndex(jd)]

    /** Tên tiết khí nếu tiết bắt đầu đúng ngày [jd], ngược lại null. */
    fun tietKhiStart(jd: Int): String? {
        val today = tietKhiIndex(jd)
        return if (today != tietKhiIndex(jd - 1)) TIET_KHI[today] else null
    }

    fun moonPhase(lunarDay: Int): String = when {
        lunarDay == 1 -> "Trăng non"
        lunarDay <= 6 -> "Trăng lưỡi liềm"
        lunarDay <= 9 -> "Trăng thượng huyền"
        lunarDay <= 13 -> "Trăng gần tròn"
        lunarDay <= 16 -> "Trăng tròn"
        lunarDay <= 24 -> "Trăng hạ huyền"
        else -> "Trăng lưỡi liềm cuối tháng"
    }

    fun lunarDayName(day: Int): String = if (day <= 10) "Mùng $day" else "Ngày $day"

    fun lunarDateText(d: LunarDate): String =
        lunarDayName(d.day) + " tháng " + MONTH_NAMES[d.month] + (if (d.leap) " nhuận" else "")

    fun lunarShort(d: LunarDate): String = "${d.day}/${d.month}" + (if (d.leap) " nhuận" else "")
}
