package top.sankokomi.wirebare.ui.resources

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun WirebareUITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    isShowStatusBar: Boolean = false,
    isShowNavigationBar: Boolean = true,
    transparentBackground: Boolean = false,
    statusBarColor: (ColorScheme) -> Color = { it.background },
    navigationBarColor: (ColorScheme) -> Color = { it.background },
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
            colorScheme = if (transparentBackground) {
                DarkColorScheme.copy(
                    background = Transparent
                )
            } else {
                DarkColorScheme
            }
        }

        else -> {
            typography = LightTypography
            colorScheme = if (transparentBackground) {
                LightColorScheme.copy(
                    background = Transparent
                )
            } else {
                LightColorScheme
            }
        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography
    ) {
        Scaffold { innerPadding ->
            RealColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isShowStatusBar) {
                    Spacer(
                        modifier = Modifier
                            .background(statusBarColor(colorScheme))
                            .fillMaxWidth()
                            .height(innerPadding.calculateTopPadding())
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
                if (isShowNavigationBar) {
                    Spacer(
                        modifier = Modifier
                            .background(navigationBarColor(colorScheme))
                            .fillMaxWidth()
                            .height(innerPadding.calculateBottomPadding())
                    )
                }
            }
        }
    }
}