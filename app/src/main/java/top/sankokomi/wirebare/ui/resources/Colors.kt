package top.sankokomi.wirebare.ui.resources

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 所有颜色都是控件的颜色，字体的颜色不包含在这里

@get:Composable
val Colors get() = MaterialTheme.colorScheme

@get:Composable
val SwitchColors
    get() = SwitchDefaults.colors(
        checkedBorderColor = Color.Transparent,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        checkedThumbColor = MaterialTheme.colorScheme.onSurface,
        uncheckedBorderColor = Color.Transparent,
        uncheckedTrackColor = MaterialTheme.colorScheme.surface,
        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
        disabledCheckedBorderColor = Color.Transparent,
        disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        disabledUncheckedBorderColor = Color.Transparent,
        disabledUncheckedTrackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    )

@get:Composable
val ReadOnlySwitchColors
    get() = SwitchDefaults.colors(
        checkedBorderColor = Color.Transparent,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        checkedThumbColor = MaterialTheme.colorScheme.onSurface,
        uncheckedBorderColor = Color.Transparent,
        uncheckedTrackColor = MaterialTheme.colorScheme.surface,
        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
        disabledCheckedBorderColor = Color.Transparent,
        disabledCheckedTrackColor = MaterialTheme.colorScheme.primary,
        disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurface,
        disabledUncheckedBorderColor = Color.Transparent,
        disabledUncheckedTrackColor = MaterialTheme.colorScheme.surface,
        disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
    )

val LightColorScheme = lightColorScheme(
    primary = LGreenA,
    onPrimary = WhiteZ,
    primaryContainer = LGreenB,
    background = LGrayA,
    onBackground = WhiteZ,
    surface = LGrayB,
    onSurface = WhiteZ,
    surfaceVariant = LGrayD,
    inverseSurface = BlackZ,
    error = RedZ,
    onError = WhiteZ,
)

val DarkColorScheme = darkColorScheme(
    primary = LGreenB,
    onPrimary = LGreenA,
    primaryContainer = LGreenD,
    background = BlackZ,
    onBackground = DGrayA,
    surface = DGrayD,
    onSurface = WhiteZ,
    surfaceVariant = DGrayF,
    inverseSurface = WhiteZ,
    error = RedZ,
    onError = BlackZ,
)
