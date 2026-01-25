package top.sankokomi.wirebare.ui.launcher

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.record.HttpRecorder
import top.sankokomi.wirebare.ui.record.HttpRsp
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.DynamicFloatImageButton
import top.sankokomi.wirebare.ui.resources.RealBox
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.RealRow
import top.sankokomi.wirebare.ui.resources.StatusBarSpacer
import top.sankokomi.wirebare.ui.resources.TextTag
import top.sankokomi.wirebare.ui.resources.Typographies
import top.sankokomi.wirebare.ui.resources.VisibleFadeInFadeOutAnimation
import top.sankokomi.wirebare.ui.util.injectTouchEffect
import top.sankokomi.wirebare.ui.wireinfo.WireInfoUI

@Composable
fun LauncherUI.PageProxyResponseResult(responseList: SnapshotStateList<HttpRsp>) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        VisibleFadeInFadeOutAnimation(responseList.isEmpty()) {
            RealColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusBarSpacer(56.dp)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.response_list_empty),
                    style = Typographies.bodyLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                StatusBarSpacer(8.dp)
                StatusBarSpacer(80.dp)
            }
        }
        if (responseList.isEmpty()) {
            return
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                StatusBarSpacer(56.dp)
            }
            items(responseList.size) { i ->
                val index = responseList.size - i - 1
                val response = responseList[index]
                val itemShape = if (responseList.size == 1) {
                    RoundedCornerShape(size = 24.dp)
                } else {
                    when (i) {
                        0 -> {
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        }

                        responseList.size - 1 -> {
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
                        RealRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .injectTouchEffect(normalBackground = Colors.onBackground) {
                                    startActivity(
                                        Intent(
                                            this@PageProxyResponseResult,
                                            WireInfoUI::class.java
                                        ).apply {
                                            putExtra("response", response)
                                        }
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            RealColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = response.url ?: response.destinationAddress ?: "NONE",
                                    modifier = Modifier.fillMaxWidth(),
                                    style = Typographies.titleMedium,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val headText = response.formatHead?.getOrNull(0)
                                if (headText != null) {
                                    Text(
                                        text = headText,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = Typographies.bodyMedium,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 2
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                RealRow {
                                    TextTag(
                                        text = if (response.isHttps == true) "SSL/TLS" else null,
                                        borderColor = Colors.primaryContainer,
                                        corner = 6.dp,
                                        space = 8.dp
                                    )
                                    TextTag(
                                        text = response.rspStatus,
                                        borderColor = Colors.primaryContainer,
                                        corner = 6.dp,
                                        space = 8.dp
                                    )
                                    TextTag(
                                        text = response.contentEncoding,
                                        borderColor = Colors.primaryContainer,
                                        corner = 6.dp,
                                        space = 8.dp
                                    )
                                    TextTag(
                                        text = response.contentType,
                                        borderColor = Colors.primaryContainer,
                                        corner = 6.dp,
                                        space = 0.dp
                                    )
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
                        HttpRecorder.clearRspRecord()
                    }
                    responseList.clear()
                }
            }
        }
    }
}
