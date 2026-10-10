package vn.itdvietnam.lichamduong.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val label: String) {
    LIGHT("Sáng"),
    DARK("Tối"),
    SYSTEM("Theo máy"),
}

enum class TextSize(val label: String, val scale: Float) {
    NORMAL("Vừa", 1.0f),
    LARGE("Lớn", 1.15f),
    XLARGE("Rất lớn", 1.3f),
}

/** Cài đặt giao diện: sáng / tối / theo máy, cỡ chữ. */
class SettingsRepository private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        ThemeMode.entries.firstOrNull { it.name == prefs.getString(KEY_THEME, null) } ?: ThemeMode.SYSTEM,
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _textSize = MutableStateFlow(
        TextSize.entries.firstOrNull { it.name == prefs.getString(KEY_TEXT, null) } ?: TextSize.LARGE,
    )
    val textSize: StateFlow<TextSize> = _textSize.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    fun setTextSize(size: TextSize) {
        _textSize.value = size
        prefs.edit().putString(KEY_TEXT, size.name).apply()
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_TEXT = "text_size"

        @Volatile
        private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context.applicationContext).also { instance = it }
            }
    }
}
