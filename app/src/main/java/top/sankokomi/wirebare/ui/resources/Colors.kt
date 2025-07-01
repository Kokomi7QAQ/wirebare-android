package top.sankokomi.wirebare.ui.resources

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 所有颜色都是控件的颜色，字体的颜色不包含在这里

@get:Composable
val Colors get() = MaterialTheme.colorScheme

@get:Composable
val SwitchColors
    get() = SwitchDefaults.colors(
        checkedBorderColor = MaterialTheme.colorScheme.primary,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        checkedThumbColor = MaterialTheme.colorScheme.onSurface,
        uncheckedBorderColor = MaterialTheme.colorScheme.surface,
        uncheckedTrackColor = MaterialTheme.colorScheme.surface,
        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface
    )

val LightColorScheme = lightColorScheme(
    primary = LGreenA,
    onPrimary = LGreenB,
    primaryContainer = WhiteZ,
    onPrimaryContainer = BlackZ,
    background = LGrayA,
    onBackground = WhiteZ,
    surface = LGrayB,
    onSurface = WhiteZ,
    surfaceVariant = LGrayD,
    error = RedZ,
    onError = WhiteZ,
)

val DarkColorScheme = darkColorScheme(
    primary = LGreenC,
    onPrimary = LGreenB,
    primaryContainer = BlackZ,
    onPrimaryContainer = WhiteZ,
    background = BlackZ,
    onBackground = DGrayA,
    surface = DGrayD,
    onSurface = WhiteZ,
    surfaceVariant = DGrayF,
    error = RedZ,
    onError = BlackZ,
)
