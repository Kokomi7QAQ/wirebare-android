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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
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
import androidx.core.view.WindowCompat
import top.sankokomi.wirebare.ui.util.navigationBarHeightDp
import top.sankokomi.wirebare.ui.util.statusBarHeightDp

private val LightColorScheme = lightColorScheme(
    primary = LGreen,
    onPrimary = MGreen,
    primaryContainer = DWhite,
    onPrimaryContainer = DBlack,
    background = LGrey,
    onBackground = DWhite,
    surface = LGrey,
    onSurface = DWhite,
    error = DRed,
    onError = DWhite,
)

private val DarkColorScheme = darkColorScheme(
    primary = DGreen,
    onPrimary = MGreen,
    primaryContainer = DBlack,
    onPrimaryContainer = DWhite,
    background = LGrey,
    onBackground = MDGrey,
    surface = LGrey,
    onSurface = MDGrey,
    error = DRed,
    onError = DBlack,
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
    val colorScheme: ColorScheme
    val typography: Typography
    when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                typography = DarkTypography
                colorScheme = dynamicDarkColorScheme(context)
            } else {
                typography = LightTypography
                colorScheme = dynamicLightColorScheme(context)
            }
        }

        darkTheme -> {
            typography = DarkTypography
            colorScheme = DarkColorScheme
        }

        else -> {
            typography = LightTypography
            colorScheme = LightColorScheme
        }
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
        typography = typography
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