package top.sankokomi.wirebare.ui.launcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.DynamicFloatImageButton
import top.sankokomi.wirebare.ui.resources.RealBox
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.RealRow
import top.sankokomi.wirebare.ui.resources.StatusBarSpacer
import top.sankokomi.wirebare.ui.resources.Typographies
import top.sankokomi.wirebare.ui.resources.VisibleFadeInFadeOutAnimation
import top.sankokomi.wirebare.ui.util.AppData
import top.sankokomi.wirebare.ui.util.injectTouchEffect
import top.sankokomi.wirebare.ui.util.selfOrNone

@Composable
fun <T> PageProxyResultList(
    emptyText: String,
    onClear: suspend () -> Unit,
    resultList: SnapshotStateList<T>,
    sourceProcessUid: (T) -> Int,
    onClick: (T) -> Unit,
    url: (T) -> String?,
    headText: (T) -> String?,
    tagContent: @Composable RowScope.(T) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        VisibleFadeInFadeOutAnimation(resultList.isEmpty()) {
            RealColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusBarSpacer(56.dp)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = emptyText,
                    style = Typographies.bodyLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                StatusBarSpacer(8.dp)
                StatusBarSpacer(80.dp)
            }
        }
        if (resultList.isEmpty()) {
            return
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                StatusBarSpacer(56.dp)
            }
            items(count = resultList.size) { i ->
                val index = resultList.size - i - 1
                val result = resultList[index]
                val itemShape = if (resultList.size == 1) {
                    RoundedCornerShape(size = 24.dp)
                } else {
                    when (i) {
                        0 -> {
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        }

                        resultList.size - 1 -> {
                            RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        }

                        else -> RectangleShape
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(itemShape)
                ) {
                    RealColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(itemShape)
                            .animateItem()
                    ) {
                        if (i != 0) {
                            RealBox {
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.2.dp),
                                    color = Colors.onBackground
                                )
                                HorizontalDivider(
                                    modifier = Modifier
                                        .padding(start = 16.dp, end = 16.dp)
                                        .fillMaxWidth()
                                        .height(0.2.dp),
                                    color = Colors.background
                                )
                            }
                        }
                        RealBox(
                            modifier = Modifier
                                .fillMaxWidth()
                                .injectTouchEffect(normalBackground = Colors.onBackground) {
                                    onClick(result)
                                }
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            RealColumn {
                                RealRow(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = remember(result) { AppData.from(sourceProcessUid(result)) },
                                        modifier = Modifier.size(16.dp),
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = remember(result) { url(result) }.selfOrNone(),
                                        modifier = Modifier.fillMaxWidth(),
                                        style = Typographies.titleMedium,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 2
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                val headText = remember(result) { headText(result) }
                                if (headText != null) {
                                    Text(
                                        text = headText,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = Typographies.bodyMedium,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                RealRow {
                                    tagContent(result)
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp)
        ) {
            val rememberScope = rememberCoroutineScope()
            DynamicFloatImageButton(
                icon = R.drawable.ic_clear
            ) {
                rememberScope.launch {
                    withContext(Dispatchers.IO) {
                        onClear()
                    }
                    resultList.clear()
                }
            }
        }
    }
}