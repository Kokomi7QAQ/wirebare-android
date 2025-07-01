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
    titleSmall = TextStyle(
        color = BlackZ,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp,
    ),
    bodySmall = TextStyle(
        color = LGrayH,
        fontSize = 10.sp,
        lineHeight = 10.sp,
    )
)

val DarkTypography = Typography(
    headlineMedium = TextStyle(
        color = WhiteZ,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    ),
    titleSmall = TextStyle(
        color = WhiteZ,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp,
    ),
    bodySmall = TextStyle(
        color = LGrayH,
        fontSize = 10.sp,
        lineHeight = 10.sp,
    )
)