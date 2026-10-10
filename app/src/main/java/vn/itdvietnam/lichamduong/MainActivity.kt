package vn.itdvietnam.lichamduong

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import vn.itdvietnam.lichamduong.settings.ThemeMode
import vn.itdvietnam.lichamduong.ui.AppViewModel
import vn.itdvietnam.lichamduong.ui.LichAppUi
import vn.itdvietnam.lichamduong.ui.theme.LichTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AppViewModel = viewModel()
            val themeMode by vm.themeMode.collectAsStateWithLifecycle()
            val textSize by vm.textSize.collectAsStateWithLifecycle()
            val dark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            // Biểu tượng thanh trạng thái theo đúng giao diện đang chọn trong ứng dụng
            DisposableEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            val windowSize = calculateWindowSizeClass(this)
            LichTheme(dark = dark, textScale = textSize.scale) {
                LichAppUi(vm = vm, widthSizeClass = windowSize.widthSizeClass)
            }
        }
    }
}
