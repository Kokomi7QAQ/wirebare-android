package top.sankokomi.wirebare.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import top.sankokomi.wirebare.ui.resources.LMGrey
import top.sankokomi.wirebare.ui.resources.MediumGrey
import kotlin.random.Random

/**
 * 用于测试 Composable 函数的重组情况，若发生了重组，背景颜色将会被改变
 * */
@Composable
fun Modifier.test(): Modifier =
    this.background(Color(Random.nextInt()))

/**
 * 将 compose 颜色转换为 Int
 * */
val Color.androidColor: Int get() = toArgb()

/**
 * 将 Int 转换为 compose 颜色
 * */
val Int.composeColor: Color get() = Color(this)

@Composable
fun Modifier.injectTouchEffect(
    touchedBackground: Color = LMGrey,
    normalBackground: Color = Color.Transparent,
    onClick: () -> Unit = {}
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val touched = interactionSource.collectIsHoveredAsState().value ||
            interactionSource.collectIsPressedAsState().value
    return this
        .background(if (touched) touchedBackground else normalBackground)
        .hoverable(interactionSource)
        .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}