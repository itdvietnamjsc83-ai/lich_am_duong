package vn.itdvietnam.lichamduong.routine

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Lưu việc lặp hằng ngày trên máy và giữ lịch báo luôn cập nhật. */
class RoutineRepository private constructor(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val scheduler = RoutineScheduler(context)
    private val items = MutableStateFlow(load())
    private val doneKeys = MutableStateFlow(loadDone())

    val routines: StateFlow<List<Routine>> = items.asStateFlow()

    /** Các lần báo đã đánh dấu "đã xong" (giữ 14 ngày gần nhất). */
    val done: StateFlow<Set<String>> = doneKeys.asStateFlow()

    fun find(id: Long): Routine? = items.value.firstOrNull { it.id == id }

    @Synchronized
    fun upsert(routine: Routine): Routine {
        val item = if (routine.id == 0L) routine.copy(id = nextId()) else routine
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

    fun rescheduleAll() = items.value.forEach { scheduler.schedule(it) }

    @Synchronized
    fun toggleDone(key: String) {
        val set = doneKeys.value.toMutableSet()
        if (!set.add(key)) set.remove(key)
        val cutoff = LocalDate.now().minusDays(14)
        set.removeAll { k -> runCatching { LocalDate.parse(k.split('|')[1]).isBefore(cutoff) }.getOrDefault(true) }
        doneKeys.value = set
        prefs.edit().putStringSet(KEY_DONE, set).apply()
    }

    // ---------- Sao lưu (định dạng chung với bản iPhone) ----------

    fun exportArray(): JSONArray = JSONArray().also { a -> items.value.forEach { a.put(toJson(it)) } }

    /** Nhập từ file sao lưu: thêm vào danh sách, bỏ qua việc trùng (cùng tên, người, giờ). */
    @Synchronized
    fun importArray(array: JSONArray): Pair<Int, Int> {
        val keys = items.value.map(::backupKey).toMutableSet()
        var added = 0
        var skipped = 0
        for (i in 0 until array.length()) {
            val r = array.optJSONObject(i)?.let { runCatching { fromJson(it).copy(id = 0L) }.getOrNull() }
            if (r == null || r.times.isEmpty()) {
                skipped++
                continue
            }
            val key = backupKey(r)
            if (!keys.add(key)) {
                skipped++
                continue
            }
            upsert(r)
            added++
        }
        return added to skipped
    }

    private fun backupKey(r: Routine) = listOf(r.title.trim(), r.who.trim(), r.times.joinToString(",")).joinToString("|")

    // ---------- Lưu trữ ----------

    private fun nextId(): Long {
        val id = prefs.getLong(KEY_NEXT_ID, 1L)
        prefs.edit().putLong(KEY_NEXT_ID, id + 1).apply()
        return id
    }

    private fun save(list: List<Routine>) {
        items.value = list.sortedBy { it.times.firstOrNull() ?: 0 }
        prefs.edit().putString(KEY_ITEMS, exportArray().toString()).apply()
    }

    private fun load(): List<Routine> {
        val raw = prefs.getString(KEY_ITEMS, null) ?: return emptyList()
        return runCatching {
            val a = JSONArray(raw)
            (0 until a.length()).map { fromJson(a.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    private fun loadDone(): Set<String> = prefs.getStringSet(KEY_DONE, emptySet())?.toSet() ?: emptySet()

    companion object {
        private const val PREFS = "routines"
        private const val KEY_ITEMS = "items"
        private const val KEY_NEXT_ID = "next_id"
        private const val KEY_DONE = "done"

        fun toJson(r: Routine): JSONObject = JSONObject()
            .put("id", r.id)
            .put("title", r.title)
            .put("who", r.who)
            .put("note", r.note)
            .put("times", JSONArray(r.times.map { RoutineSchedule.format(it) }))
            .put("days", JSONArray(r.days.sorted()))
            .put("start", r.startDate.toString())
            .put("end", r.endDate?.toString() ?: JSONObject.NULL)
            .put("ring", r.ring)
            .put("snooze", r.snoozeMinutes)
            .put("on", r.enabled)

        fun fromJson(o: JSONObject): Routine {
            val t = o.optJSONArray("times") ?: JSONArray()
            val times = RoutineSchedule.normalize((0 until t.length()).flatMap { RoutineSchedule.parseTimes(t.optString(it)) })
            val d = o.optJSONArray("days")
            val days = if (d == null) Routine.ALL_DAYS else (0 until d.length()).map { d.optInt(it) }.filter { it in 1..7 }.toSet()
            fun date(key: String): LocalDate? =
                if (o.isNull(key)) null else runCatching { LocalDate.parse(o.optString(key)) }.getOrNull()
            return Routine(
                id = o.optLong("id", 0L),
                title = o.optString("title", "Việc hằng ngày").take(120).ifBlank { "Việc hằng ngày" },
                who = o.optString("who", "").take(60),
                note = o.optString("note", "").take(200),
                times = times,
                days = days.ifEmpty { Routine.ALL_DAYS },
                startDate = date("start") ?: LocalDate.now(),
                endDate = date("end"),
                ring = o.optBoolean("ring", true),
                snoozeMinutes = o.optInt("snooze", 10).coerceIn(1, 60),
                enabled = o.optBoolean("on", true),
            )
        }

        @Volatile
        private var instance: RoutineRepository? = null

        fun get(context: Context): RoutineRepository =
            instance ?: synchronized(this) {
                instance ?: RoutineRepository(context.applicationContext).also { instance = it }
            }
    }
}
