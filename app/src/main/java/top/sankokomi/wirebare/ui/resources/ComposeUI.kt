package top.sankokomi.wirebare.ui.resources

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.util.injectTouchEffect
import top.sankokomi.wirebare.ui.util.statusBarHeightDp

@Composable
fun AppStatusBar(color: Color = Color.Transparent) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(statusBarHeightDp)
            .background(color)
    )
}

@Composable
fun AppTitleBar(
    icon: Any? = null,
    text: String = stringResource(id = R.string.app_name),
    startContent: @Composable BoxScope.() -> Unit = {},
    endContent: @Composable BoxScope.() -> Unit = {}
) {
    RealColumn(
        modifier = Modifier.background(
            Brush.verticalGradient(
                listOf(
                    LightGrey,
                    Color.Transparent
                )
            )
        )
    ) {
        RealBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            RealBox(
                modifier = Modifier.align(Alignment.CenterStart),
                content = startContent
            )
            RealRow(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    AsyncImage(
                        model = icon,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            RealBox(
                modifier = Modifier.align(Alignment.CenterEnd),
                content = endContent
            )
        }
    }
}

@Stable
class CheckableMenuItem(
    val itemName: State<String>,
    val checked: MutableState<Boolean>,
    val icon: Any
)

@Composable
fun AppCheckableMenu(
    itemList: List<CheckableMenuItem>,
    size: Int = itemList.size
) {
    RealColumn {
        for (i in 0 until size) {
            val item = itemList[i]
            val name = item.itemName
            val checked = item.checked
            val icon = item.icon
            RealColumn {
                if (i != 0) {
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(start = 48.dp, end = 16.dp)
                            .fillMaxWidth()
                            .height(0.2.dp)
                            .background(LightGrey)
                    )
                }
                RealRow(
                    modifier = Modifier
                        .injectTouchEffect(normalBackground = Color.White)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = icon,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(16.dp),
                        contentDescription = null
                    )
                    Text(
                        text = name.value,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        color = Color.Black
                    )
                    Switch(
                        checked = checked.value,
                        thumbContent = {
                            Spacer(modifier = Modifier.size(999.dp))
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MediumGreen,
                            uncheckedBorderColor = LightGrey,
                            uncheckedTrackColor = LightGrey,
                            uncheckedThumbColor = Color.White
                        ),
                        onCheckedChange = {
                            checked.value = !checked.value
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppRoundCornerBox(
    background: Color = Color.White,
    content: @Composable BoxScope.() -> Unit
) {
    RealBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(background),
        content = content
    )
}

@Composable
fun Tag(
    borderColor: Color,
    corner: Dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(corner))
            .background(borderColor)
            .padding(2.dp),
        content = content
    )
}

@Composable
fun ImageButton(
    painter: Painter,
    clickable: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val touched = interactionSource.collectIsPressedAsState().value ||
            interactionSource.collectIsHoveredAsState().value
    val anim = remember { Animatable(initialValue = 40f) }
    LaunchedEffect(touched) {
        anim.stop()
        anim.animateTo(
            targetValue = if (touched) 64f else 48f,
            animationSpec = spring(
                dampingRatio = 0.4f,
                stiffness = 800f
            )
        )
    }
    Box(
        modifier = Modifier.requiredSize(80.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(anim.value.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = clickable
                )
                .background(LightGreen),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painter,
                modifier = Modifier.size((0.6f * anim.value).dp),
                contentDescription = null
            )
        }
    }
}

@Composable
fun VisibleFadeInFadeOutAnimation(
    visible: Boolean = true,
    content: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        content = content
    )
}

@Composable
fun VisibleDynamicEffectAnimation(
    visible: Boolean = true,
    content: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(50)
        ) + scaleIn(
            animationSpec = tween(50),
            initialScale = 1.01f,
            transformOrigin = TransformOrigin(0.5f, 1f)
        ),
        exit = fadeOut(
            animationSpec = tween(50)
        ) + scaleOut(
            animationSpec = tween(50),
            targetScale = 1.01f,
            transformOrigin = TransformOrigin(0.5f, 1f)
        ),
        content = content
    )
}

@Composable
fun SmallColorfulText(
    mainText: String,
    subText: String,
    backgroundColor: Color,
    textColor: Color
) {
    RealColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clip(RoundedCornerShape(6.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = mainText,
            modifier = Modifier
                .fillMaxWidth(),
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 15.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subText,
            modifier = Modifier
                .fillMaxWidth(),
            color = textColor,
            fontSize = 12.sp,
            lineHeight = 13.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LargeColorfulText(
    mainText: String,
    subText: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    RealColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clip(RoundedCornerShape(6.dp))
            .combinedClickable(onLongClick = onLongClick, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = mainText,
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = subText, color = textColor, fontSize = 14.sp, lineHeight = 16.sp)
    }
}

@Composable
fun CornerSlideBar(
    mainText: String,
    subText: String,
    backgroundColor: Color,
    textColor: Color,
    barColor: Color,
    barBackgroundColor: Color,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit = {},
    valueText: (Float) -> String = { "${(it * 100).toInt()}%" }
) {
    RealColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clip(RoundedCornerShape(6.dp))
            .padding(vertical = 12.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = mainText,
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp
        )
        RealRow {
            Text(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .align(Alignment.Bottom),
                text = subText,
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = valueText(value),
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        RealBox {
            Slider(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(32.dp)
                    .alpha(0f),
                value = value,
                valueRange = valueRange,
                onValueChange = onValueChange
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(barBackgroundColor)
            )
            Row {
                val percentage = value / (valueRange.endInclusive - valueRange.start)
                if (percentage > 0f) {
                    Spacer(modifier = Modifier.weight(percentage))
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(barColor)
                )
                if (percentage < 1f) {
                    Spacer(modifier = Modifier.weight(1 - percentage))
                }
            }
        }
    }
}

@Composable
fun RealBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier, contentAlignment, propagateMinConstraints, content)
}

@Composable
fun RealColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier, verticalArrangement, horizontalAlignment, content)
}

@Composable
fun RealRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    Row(modifier, horizontalArrangement, verticalAlignment, content)
}