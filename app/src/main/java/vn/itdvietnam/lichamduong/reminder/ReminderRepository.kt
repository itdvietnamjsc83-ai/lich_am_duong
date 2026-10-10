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
        )
    }

    companion object {
        private const val PREFS = "reminders"
        private const val KEY_ITEMS = "items"
        private const val KEY_NEXT_ID = "next_id"

        @Volatile
        private var instance: ReminderRepository? = null

        fun get(context: Context): ReminderRepository =
            instance ?: synchronized(this) {
                instance ?: ReminderRepository(context.applicationContext).also { instance = it }
            }
    }
}
