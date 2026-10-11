package vn.itdvietnam.lichamduong.reminder

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Lưu nhắc lịch trên máy (SharedPreferences, dạng JSON) và giữ lịch báo luôn cập nhật. */
class ReminderRepository private constructor(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val scheduler = ReminderScheduler(context)
    private val items = MutableStateFlow(loadOrSeed())

    val reminders: StateFlow<List<Reminder>> = items.asStateFlow()

    fun find(id: Long): Reminder? = items.value.firstOrNull { it.id == id }

    /** Thêm mới (khi id = 0) hoặc cập nhật, rồi đặt lại lịch báo. */
    @Synchronized
    fun upsert(reminder: Reminder): Reminder {
        val item = if (reminder.id == 0L) reminder.copy(id = nextId()) else reminder
        save(items.value.filterNot { it.id == item.id } + item)
        scheduler.schedule(item)
        return item
    }

    @Synchronized
    fun delete(id: Long) {
        scheduler.cancel(id)
        save(items.value.filterNot { it.id == id })
    }

    fun setEnabled(id: Long, enabled: Boolean) {
        find(id)?.let { upsert(it.copy(enabled = enabled)) }
    }

    fun rescheduleAll() {
        items.value.forEach { scheduler.schedule(it) }
    }

    // ---------- Sao lưu & chuyển máy ----------
    // Định dạng chung với bản web cho iPhone, nên chuyển qua lại Android ↔ iPhone được.

    data class ImportResult(val added: Int, val skipped: Int)

    fun exportJson(): String {
        val array = JSONArray()
        items.value.forEach { r ->
            array.put(
                JSONObject()
                    .put("title", r.title)
                    .put("cal", if (r.isLunar) "lunar" else "solar")
                    .put("rep", when (r.repeat) { RepeatType.ONCE -> "once"; RepeatType.MONTHLY -> "month"; RepeatType.YEARLY -> "year" })
                    .put("d", r.day)
                    .put("m", r.month)
                    .put("y", r.year)
                    .put("extra", JSONArray(r.extraDays))
                    .put("before", r.beforeDays)
                    .put("hour", r.hour)
                    .put("minute", r.minute)
                    .put("on", r.enabled)
                    .put("alarm", r.alarm),
            )
        }
        return JSONObject()
            .put("app", BACKUP_APP)
            .put("format", 1)
            .put("exportedAt", java.time.OffsetDateTime.now().toString())
            .put("reminders", array)
            .toString(2)
    }

    /** Nhập từ file sao lưu: thêm vào danh sách hiện có, bỏ qua nhắc trùng. Ném lỗi nếu file sai định dạng. */
    @Synchronized
    fun importJson(text: String): ImportResult {
        val root = JSONObject(text)
        require(root.optString("app") == BACKUP_APP) { "Không phải file sao lưu Lịch Âm Dương" }
        val array = root.getJSONArray("reminders")
        val keys = items.value.map(::backupKey).toMutableSet()
        var added = 0
        var skipped = 0
        for (i in 0 until array.length()) {
            val o = array.optJSONObject(i)
            val calendar = when (o?.optString("cal")) { "lunar" -> CalendarType.LUNAR; "solar" -> CalendarType.SOLAR; else -> null }
            val repeat = when (o?.optString("rep")) { "once" -> RepeatType.ONCE; "month" -> RepeatType.MONTHLY; "year" -> RepeatType.YEARLY; else -> null }
            if (o == null || calendar == null || repeat == null) {
                skipped++
                continue
            }
            val extra = o.optJSONArray("extra")
            val before = o.optInt("before", 0)
            val reminder = Reminder(
                id = 0L,
                title = o.optString("title", "Nhắc lịch").take(120).ifBlank { "Nhắc lịch" },
                calendar = calendar,
                repeat = repeat,
                day = o.optInt("d", 1).coerceIn(1, if (calendar == CalendarType.LUNAR) 30 else 31),
                month = o.optInt("m", 1).coerceIn(1, 12),
                year = o.optInt("y", 2026).coerceIn(1900, 2100),
                extraDays = if (extra == null) emptyList() else (0 until minOf(extra.length(), 1)).map { extra.optInt(it, 1).coerceIn(1, 31) },
                beforeDays = if (before in setOf(0, 1, 3, 7)) before else 0,
                hour = o.optInt("hour", 7).coerceIn(0, 23),
                minute = o.optInt("minute", 0).coerceIn(0, 59),
                enabled = o.optBoolean("on", true),
                alarm = o.optBoolean("alarm", false),
            )
            val key = backupKey(reminder)
            if (key in keys) {
                skipped++
                continue
            }
            keys.add(key)
            upsert(reminder)
            added++
        }
        return ImportResult(added, skipped)
    }

    private fun backupKey(r: Reminder): String = listOf(
        r.title, r.calendar.name, r.repeat.name, r.day,
        if (r.repeat == RepeatType.MONTHLY) "" else r.month,
        if (r.repeat == RepeatType.ONCE) r.year else "",
        r.extraDays.joinToString("+"),
    ).joinToString("|")

    private fun nextId(): Long {
        val id = prefs.getLong(KEY_NEXT_ID, 1L)
        prefs.edit().putLong(KEY_NEXT_ID, id + 1).apply()
        return id
    }

    private fun save(list: List<Reminder>) {
        items.value = list
        val array = JSONArray()
        list.forEach { array.put(toJson(it)) }
        prefs.edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    private fun loadOrSeed(): List<Reminder> {
        val raw = prefs.getString(KEY_ITEMS, null)
        if (raw == null) {
            val seed = Reminder(
                id = 1L,
                title = "Mùng 1 & Rằm",
                calendar = CalendarType.LUNAR,
                repeat = RepeatType.MONTHLY,
                day = 1, month = 1, year = 2026,
                extraDays = listOf(15),
                hour = 6, minute = 0,
            )
            val array = JSONArray().put(toJson(seed))
            prefs.edit().putString(KEY_ITEMS, array.toString()).putLong(KEY_NEXT_ID, 2L).apply()
            scheduler.schedule(seed)
            return listOf(seed)
        }
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { fromJson(array.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    private fun toJson(r: Reminder): JSONObject = JSONObject()
        .put("id", r.id)
        .put("title", r.title)
        .put("calendar", r.calendar.name)
        .put("repeat", r.repeat.name)
        .put("day", r.day)
        .put("month", r.month)
        .put("year", r.year)
        .put("extraDays", JSONArray(r.extraDays))
        .put("beforeDays", r.beforeDays)
        .put("hour", r.hour)
        .put("minute", r.minute)
        .put("enabled", r.enabled)
        .put("alarm", r.alarm)

    private fun fromJson(o: JSONObject): Reminder {
        val extra = o.optJSONArray("extraDays")
        return Reminder(
            id = o.getLong("id"),
            title = o.optString("title", "Nhắc lịch"),
            calendar = CalendarType.entries.firstOrNull { it.name == o.optString("calendar") } ?: CalendarType.LUNAR,
            repeat = RepeatType.entries.firstOrNull { it.name == o.optString("repeat") } ?: RepeatType.YEARLY,
            day = o.optInt("day", 1),
            month = o.optInt("month", 1),
            year = o.optInt("year", 2026),
            extraDays = if (extra == null) emptyList() else (0 until extra.length()).map { extra.getInt(it) },
            beforeDays = o.optInt("beforeDays", 0),
            hour = o.optInt("hour", 7),
            minute = o.optInt("minute", 0),
            enabled = o.optBoolean("enabled", true),
            alarm = o.optBoolean("alarm", false),
        )
    }

    companion object {
        private const val PREFS = "reminders"
        private const val KEY_ITEMS = "items"
        private const val KEY_NEXT_ID = "next_id"
        private const val BACKUP_APP = "lich-am-duong"

        @Volatile
        private var instance: ReminderRepository? = null

        fun get(context: Context): ReminderRepository =
            instance ?: synchronized(this) {
                instance ?: ReminderRepository(context.applicationContext).also { instance = it }
            }
    }
}
