package top.sankokomi.wirebare.ui.resources

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import top.sankokomi.wirebare.ui.util.hideNavigationBar
import top.sankokomi.wirebare.ui.util.hideStatusBar
import top.sankokomi.wirebare.ui.util.navigationBarHeightDp
import top.sankokomi.wirebare.ui.util.showNavigationBar
import top.sankokomi.wirebare.ui.util.showStatusBar
import top.sankokomi.wirebare.ui.util.statusBarHeightDp

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun WirebareUITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    isShowStatusBar: Boolean = false,
    isShowNavigationBar: Boolean = true,
    statusBarColor: Color = Color.Black,
    navigationBarColor: Color = Color.Black,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        Column {
            if (isShowStatusBar) {
                Spacer(
                    modifier = Modifier
                        .background(statusBarColor)
                        .fillMaxWidth()
                        .height(statusBarHeightDp)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            if (isShowNavigationBar) {
                Spacer(
                    modifier = Modifier
                        .background(navigationBarColor)
                        .fillMaxWidth()
                        .height(navigationBarHeightDp)
                )
            }
        }
    }
}