package top.sankokomi.wirebare.ui.wireinfo

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import top.sankokomi.wirebare.kernel.common.WireBareHelper
import top.sankokomi.wirebare.kernel.net.IpVersion
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.record.HttpReq
import top.sankokomi.wirebare.ui.record.HttpRsp
import top.sankokomi.wirebare.ui.resources.AppExpandableItem
import top.sankokomi.wirebare.ui.resources.AppExpandableRichItem
import top.sankokomi.wirebare.ui.resources.AppRoundCornerBox
import top.sankokomi.wirebare.ui.resources.AppStatusBar
import top.sankokomi.wirebare.ui.resources.AppTitleBar
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.LGreenA
import top.sankokomi.wirebare.ui.resources.LargeColorfulText
import top.sankokomi.wirebare.ui.resources.RealBox
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.Typographies
import top.sankokomi.wirebare.ui.util.copyTextToClipBoard
import top.sankokomi.wirebare.ui.util.showToast
import top.sankokomi.wirebare.ui.util.statusBarHeightDp
import kotlin.math.min

@Composable
fun WireInfoUI.WireInfoUIPage(
    request: HttpReq,
    sessionId: String
) {
    RealBox {
        RealColumn(
            modifier = Modifier.zIndex(1f)
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarHeightDp)
                    .background(Colors.background)
            )
            AppTitleBar(text = stringResource(R.string.request_info_title))
        }
        var isUrlExpand by remember { mutableStateOf(false) }
        var isOriginHeaderExpand by remember { mutableStateOf(false) }
        LazyColumn {
            item {
                Spacer(modifier = Modifier.height(56.dp + statusBarHeightDp))
            }
            item {
                AppRoundCornerBox(
                    background = Colors.primary
                ) {
                    AppExpandableItem(
                        icon = R.drawable.ic_link,
                        title = stringResource(R.string.request_info_url),
                        body = request.url ?: "",
                        expand = isUrlExpand,
                        onLongClick = {
                            copyTextToClipBoard(request.url ?: "")
                            showToast(R.string.request_info_copy_success)
                        }
                    ) {
                        isUrlExpand = !it
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                AppRoundCornerBox(
                    background = Colors.primary
                ) {
                    val ipVersion = remember {
                        WireBareHelper.parseIpVersion(request.destinationAddress)
                    }
                    val strIpVersion = remember {
                        when (ipVersion) {
                            IpVersion.IPv4 -> "IPv4"
                            IpVersion.IPv6 -> "IPv6"
                            else -> ""
                        }
                    }
                    val srcIp = remember {
                        when (ipVersion) {
                            IpVersion.IPv4 -> "127.0.0.1"
                            IpVersion.IPv6 -> "::1"
                            else -> ""
                        }
                    }
                    val srcPort = remember {
                        request.sourcePort?.toUShort()?.toString() ?: ""
                    }
                    val destIp = remember {
                        request.destinationAddress?.toString() ?: ""
                    }
                    val destPort = remember {
                        request.destinationPort?.toUShort()?.toString() ?: ""
                    }
                    AppExpandableRichItem(
                        icon = R.drawable.ic_address,
                        title = stringResource(R.string.request_info_ip),
                        expandable = false
                    ) {
                        RealColumn {
                            TextChapter(
                                title = stringResource(R.string.request_info_ip_version),
                                content = strIpVersion
                            )
                            TextChapter(
                                title = stringResource(R.string.request_info_ip_src),
                                content = "$srcIp:$srcPort"
                            )
                            TextChapter(
                                title = stringResource(R.string.request_info_ip_dest),
                                content = "$destIp:$destPort",
                                appendNewLine = false
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                AppRoundCornerBox(
                    background = Colors.primary
                ) {
                    AppExpandableRichItem(
                        icon = R.drawable.ic_api,
                        title = stringResource(R.string.request_info_req_line),
                        expandable = false
                    ) {
                        RealColumn {
                            TextChapter(
                                title = stringResource(R.string.request_info_method),
                                content = request.method ?: ""
                            )
                            TextChapter(
                                title = stringResource(R.string.request_info_http_version),
                                content = request.httpVersion ?: "",
                                appendNewLine = false
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                if (request.formatHead == null || request.formatHead.size < 2) {
                    return@item
                }
                val headerList = remember {
                    request.formatHead.subList(1, request.formatHead.size).map {
                        val splitIndex = it.indexOf(": ")
                        if (splitIndex < 0) return@map "" to it
                        return@map it.substring(
                            0,
                            splitIndex
                        ) to it.substring(
                            splitIndex + 2,
                            it.length
                        )
                    }
                }
                AppRoundCornerBox(
                    background = Colors.primary
                ) {
                    val expandable = headerList.size > 4
                    AppExpandableRichItem(
                        icon = R.drawable.ic_wirebare,
                        title = stringResource(R.string.request_info_headers),
                        expandable = expandable,
                        expand = isOriginHeaderExpand,
                        onExpandChanged = { isOriginHeaderExpand = !it }
                    ) content@{ expand ->
                        RealColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            val maxLines = min(headerList.size, if (expand) headerList.size else 4)
                            for (index in 0 until maxLines) {
                                val header = headerList[index]
                                val title = header.first
                                val content = header.second
                                TextChapter(
                                    title = title,
                                    content = content,
                                    appendNewLine = index != maxLines - 1
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                RealColumn(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    DataViewer(sessionId)
                }
            }
        }
    }
}

@Composable
fun WireInfoUI.WireInfoUIPage(
    response: HttpRsp,
    sessionId: String
) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 4.dp)
        ) {
            AppStatusBar()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "目的 IP 地址",
                    subText = response.destinationAddress ?: "",
                    backgroundColor = LGreenA,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "来源端口",
                    subText = response.sourcePort?.toUShort()?.toString() ?: "",
                    backgroundColor = LGreenA,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "目的端口",
                    subText = response.destinationPort?.toUShort()?.toString() ?: "",
                    backgroundColor = LGreenA,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "URL 链接",
                    subText = response.url ?: "",
                    backgroundColor = LGreenA,
                    textColor = Color.Black,
                    onLongClick = {
                        val url = response.url
                        if (!url.isNullOrBlank()) {
                            copyTextToClipBoard(url)
                            showToast("已复制 URL")
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "HTTP 版本",
                    subText = response.httpVersion ?: "",
                    backgroundColor = LGreenA,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "HTTP 响应状态码",
                    subText = response.rspStatus ?: "",
                    backgroundColor = LGreenA,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "INTERNAL SESSION ID",
                    subText = sessionId,
                    backgroundColor = LGreenA,
                    textColor = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "HTTP 响应头",
                    subText = response.formatHead?.joinToString("\n\n") ?: "",
                    backgroundColor = LGreenA,
                    textColor = Color.Black
                )
            }
            DataViewer(sessionId)
        }
    }
}

@Composable
private fun WireInfoUI.DataViewer(sessionId: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    ) {
        LargeColorfulText(
            mainText = "解析为 HTML",
            subText = "将报文作为 HTML 文本进行解析",
            backgroundColor = LGreenA,
            textColor = Color.Black,
            onClick = {
                startActivity(
                    Intent(
                        this@DataViewer,
                        WireDetailUI::class.java
                    ).apply {
                        putExtra("detail_mode", DetailMode.DirectHtml.ordinal)
                        putExtra("session_id", sessionId)
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    ) {
        LargeColorfulText(
            mainText = "gzip 解压缩并解析为 HTML（TESTING）",
            subText = "将报文作为被 gzip 压缩的 HTML 文本进行解析",
            backgroundColor = LGreenA,
            textColor = Color.Black,
            onClick = {
                startActivity(
                    Intent(
                        this@DataViewer,
                        WireDetailUI::class.java
                    ).apply {
                        putExtra("detail_mode", DetailMode.GzipHtml.ordinal)
                        putExtra("session_id", sessionId)
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    ) {
        LargeColorfulText(
            mainText = "brotli 解压缩并解析为 HTML（TESTING）",
            subText = "将报文作为被 brotli 压缩的 HTML 文本进行解析",
            backgroundColor = LGreenA,
            textColor = Color.Black,
            onClick = {
                startActivity(
                    Intent(
                        this@DataViewer,
                        WireDetailUI::class.java
                    ).apply {
                        putExtra("detail_mode", DetailMode.BrotliHtml.ordinal)
                        putExtra("session_id", sessionId)
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    ) {
        LargeColorfulText(
            mainText = "解析为图片",
            subText = "将报文作为图片数据进行解析",
            backgroundColor = LGreenA,
            textColor = Color.Black,
            onClick = {
                startActivity(
                    Intent(
                        this@DataViewer,
                        WireDetailUI::class.java
                    ).apply {
                        putExtra("detail_mode", DetailMode.DirectImage.ordinal)
                        putExtra("session_id", sessionId)
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    ) {
        LargeColorfulText(
            mainText = "gzip 解压缩并解析为图片（TESTING）",
            subText = "将报文作为被 gzip 压缩的图片数据进行解析",
            backgroundColor = LGreenA,
            textColor = Color.Black,
            onClick = {
                startActivity(
                    Intent(
                        this@DataViewer,
                        WireDetailUI::class.java
                    ).apply {
                        putExtra("detail_mode", DetailMode.GzipImage.ordinal)
                        putExtra("session_id", sessionId)
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
    ) {
        LargeColorfulText(
            mainText = "brotli 解压缩并解析为图片（TESTING）",
            subText = "将报文作为被 brotli 压缩的图片数据进行解析（测试中）",
            backgroundColor = LGreenA,
            textColor = Color.Black,
            onClick = {
                startActivity(
                    Intent(
                        this@DataViewer,
                        WireDetailUI::class.java
                    ).apply {
                        putExtra("detail_mode", DetailMode.BrotliImage.ordinal)
                        putExtra("session_id", sessionId)
                    }
                )
            }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
@Suppress("ComposableNaming")
private fun TextChapter(
    title: String,
    content: String,
    appendNewLine: Boolean = true,
    autoSummary: Boolean = true
) {
    Text(
        text = title,
        style = Typographies.titleLarge
    )
    if (content.length <= 100 || !autoSummary) {
        Text(
            text = content + if (appendNewLine) System.lineSeparator() else "",
            modifier = Modifier.fillMaxWidth(),
            style = Typographies.bodyLarge
        )
    } else {
        val context = LocalContext.current
        Text(
            text = stringResource(
                R.string.request_info_content_too_long
            ).format(content.length) + if (appendNewLine) System.lineSeparator() else "",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    context.startActivity(
                        Intent(context, WireDetailPopupUI::class.java).also {
                            it.putExtra("title", title)
                            it.putExtra("content", content)
                        }
                    )
                },
            style = Typographies.bodyLarge
        )
    }
}
