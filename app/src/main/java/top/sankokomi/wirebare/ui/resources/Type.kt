package top.sankokomi.wirebare.ui.resources

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val LightTypography = Typography(
    titleLarge = TextStyle(
        color = DBlack,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
)

val DarkTypography = Typography(
    titleLarge = TextStyle(
        color = DWhite,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
)