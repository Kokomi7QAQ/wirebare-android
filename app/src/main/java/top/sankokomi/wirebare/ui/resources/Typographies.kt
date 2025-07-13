package top.sankokomi.wirebare.ui.resources

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@get:Composable
val Typographies get() = MaterialTheme.typography

val LightTypography = Typography(
    headlineMedium = TextStyle(
        color = BlackZ,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = TextStyle(
        color = DGrayH,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = TextStyle(
        color = BlackZ,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp,
    ),
    titleSmall = TextStyle(
        color = BlackZ,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
    ),
    bodyMedium = TextStyle(
        color = DGrayD,
        fontSize = 14.sp,
        lineHeight = 14.sp,
    ),
    bodySmall = TextStyle(
        color = LGrayH,
        fontSize = 13.sp,
        lineHeight = 13.sp,
    ),
    labelMedium = TextStyle(
        color = DGrayC,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
)

val DarkTypography = Typography(
    headlineMedium = TextStyle(
        color = WhiteZ,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = TextStyle(
        color = LGrayH,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = TextStyle(
        color = WhiteZ,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp,
    ),
    titleSmall = TextStyle(
        color = WhiteZ,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
    ),
    bodyMedium = TextStyle(
        color = LGrayD,
        fontSize = 14.sp,
        lineHeight = 14.sp,
    ),
    bodySmall = TextStyle(
        color = LGrayH,
        fontSize = 13.sp,
        lineHeight = 13.sp,
    ),
    labelMedium = TextStyle(
        color = DGrayC,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
)