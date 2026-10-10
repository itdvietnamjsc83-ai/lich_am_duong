package vn.itdvietnam.lichamduong.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density

/** Bảng màu sáng: nền trắng, màu nhấn đỏ son. */
private val LightColors = lightColorScheme(
    primary = Color(0xFFB42318),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCEBE9),
    onPrimaryContainer = Color(0xFF7A1A12),
    secondary = Color(0xFF8A5A00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFBF1DC),
    onSecondaryContainer = Color(0xFF5C3B00),
    background = Color.White,
    onBackground = Color(0xFF1C1B1A),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1A),
    surfaceVariant = Color(0xFFF5F3F0),
    onSurfaceVariant = Color(0xFF5F5A55),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAF9F7),
    surfaceContainer = Color(0xFFF5F3F0),
    surfaceContainerHigh = Color(0xFFF0EDE9),
    surfaceContainerHighest = Color(0xFFEAE6E1),
    outline = Color(0xFFD9D4CE),
    outlineVariant = Color(0xFFE3DFDA),
)

/**
 * Bảng màu tối cho màn hình AMOLED: nền đen tuyền (#000000) để điểm ảnh tắt hẳn, tiết kiệm pin.
 * Màu nhấn được làm sáng để giữ độ tương phản; chữ trên nền nhấn dùng màu đen.
 */
private val DarkAmoledColors = darkColorScheme(
    primary = Color(0xFFF4796B),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF2A1512),
    onPrimaryContainer = Color(0xFFFFB4A9),
    secondary = Color(0xFFE0A84A),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2A2010),
    onSecondaryContainer = Color(0xFFF5D9A8),
    background = Color.Black,
    onBackground = Color(0xFFF2F0ED),
    surface = Color.Black,
    onSurface = Color(0xFFF2F0ED),
    surfaceVariant = Color(0xFF141414),
    onSurfaceVariant = Color(0xFFA8A29C),
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF141414),
    surfaceContainerHigh = Color(0xFF1F1F1F),
    surfaceContainerHighest = Color(0xFF2A2A2A),
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF2E2E2E),
)

/** Phông có chân dùng cho các con số lớn (Noto Serif trên Android, hỗ trợ đầy đủ tiếng Việt). */
val NumberFont: FontFamily = FontFamily.Serif

@Composable
fun LichTheme(dark: Boolean, textScale: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * textScale),
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkAmoledColors else LightColors,
            typography = Typography(),
            content = content,
        )
    }
}
