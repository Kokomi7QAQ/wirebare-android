package top.sankokomi.wirebare.ui.wireinfo

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.kernel.common.WireBareHelper
import top.sankokomi.wirebare.kernel.net.IpVersion
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.record.HttpReq
import top.sankokomi.wirebare.ui.record.HttpRsp
import top.sankokomi.wirebare.ui.record.httpBody
import top.sankokomi.wirebare.ui.record.httpRoom
import top.sankokomi.wirebare.ui.record.readHttpBytesById
import top.sankokomi.wirebare.ui.resources.AppExpandableRichItem
import top.sankokomi.wirebare.ui.resources.AppExpandableTextItem
import top.sankokomi.wirebare.ui.resources.AppMenuItem
import top.sankokomi.wirebare.ui.resources.AppRoundCornerBox
import top.sankokomi.wirebare.ui.resources.AppTab
import top.sankokomi.wirebare.ui.resources.AppTitleBar
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.RealBox
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.TabData
import top.sankokomi.wirebare.ui.resources.Typographies
import top.sankokomi.wirebare.ui.util.copyTextToClipBoard
import top.sankokomi.wirebare.ui.util.showToast
import top.sankokomi.wirebare.ui.util.statusBarHeightDp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.min

@Composable
fun WireInfoUI.WireInfoUIPage(
    req: HttpReq?,
    rsp: HttpRsp?
) {
    var request = req
    var response = rsp
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            when {
                req != null -> {
                    httpRoom.httpDao().queryRspId(req.id)?.let { rspId ->
                        response = httpRoom.httpDao().queryHttpRspById(rspId).firstOrNull()
                    }
                }

                rsp != null -> {
                    httpRoom.httpDao().queryReqId(rsp.id)?.let { reqId ->
                        request = httpRoom.httpDao().queryHttpReqById(reqId).firstOrNull()
                    }
                }
            }
        }
    }
    val strRequestInfo = stringResource(R.string.request_info_title)
    val strResponseInfo = stringResource(R.string.response_info_title)
    val strFormatter = stringResource(R.string.req_rsp_info_formatter)
    val tabDataList = remember {
        listOf(
            TabData(R.drawable.ic_request, strRequestInfo),
            TabData(R.drawable.ic_response, strResponseInfo),
            TabData(R.drawable.ic_format, strFormatter)
        )
    }
    val pagerState = rememberPagerState { tabDataList.size }
    val titleBarText = remember { mutableStateOf(tabDataList.first().text) }
    val anim = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        if (rsp != null) {
            titleBarText.value = tabDataList[1].text
            pagerState.animateScrollToPage(
                1,
                animationSpec = tween(0)
            )
        }
    }
    RealBox(modifier = Modifier.background(Colors.background)) {
        RealColumn(
            modifier = Modifier.zIndex(1f)
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarHeightDp)
                    .background(Colors.background)
            )
            AppTitleBar(text = titleBarText.value)
        }
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            beyondViewportPageCount = 3,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = ((1f - anim.value) * 4).dp)
                .alpha(1f - ((1f - anim.value)))
        ) {
            when (it) {
                0 -> WireInfoRequestUIPage(request)

                1 -> WireInfoResponseUIPage(response)

                2 -> WireInfoFormatterUIPage(
                    contentEncoding = response?.contentEncoding ?: "",
                    contentType = response?.contentType ?: "",
                    sessionId = response?.id ?: ""
                )
            }
        }
        val rememberScope = rememberCoroutineScope()
        RealBox(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Colors.background
                        )
                    )
                )
        ) {
            AppTab(tabDataList) { index ->
                if (pagerState.currentPage != index) {
                    rememberScope.launch {
                        anim.stop()
                        anim.animateTo(0f, tween(100))
                        pagerState.animateScrollToPage(
                            index,
                            animationSpec = tween(0)
                        )
                        titleBarText.value = tabDataList[index].text
                        anim.animateTo(1f, tween(100))
                    }
                }
            }
        }
    }
}

@Composable
private fun WireInfoUI.WireInfoFormatterUIPage(
    contentEncoding: String,
    contentType: String,
    sessionId: String
) {
    val compressAlgorithm = remember {
        when {
            contentEncoding.contains("gzip", true) -> DecompressType.Gzip.code
            contentEncoding.contains("br", true) -> DecompressType.Brotli.code
            else -> DecompressType.None.code
        }
    }
    val contentType = remember {
        when {
            contentType.startsWith("image/", true) -> ContentType.Image.code
            contentType.startsWith("text/html", true) -> ContentType.Html.code
            contentType.startsWith("text/", true) ||
                    contentType.startsWith("application/", true) -> ContentType.Text.code

            else -> DecompressType.None.code
        }
    }
    RealColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        var selectedDecompress by remember { mutableIntStateOf(compressAlgorithm) }
        var selectedFormatter by remember { mutableIntStateOf(contentType) }
        Spacer(modifier = Modifier.height(56.dp + statusBarHeightDp))
        AppRoundCornerBox {
            val strNone = stringResource(R.string.req_rsp_info_none)
            val strGzip = stringResource(R.string.req_rsp_info_gzip)
            val strBrotli = stringResource(R.string.req_rsp_info_brotli)
            AppMenuItem(
                icon = null,
                itemName = stringResource(R.string.req_rsp_info_decompress_algorithm),
                selected = selectedDecompress,
                selectableList = remember { listOf(strNone, strGzip, strBrotli) },
                subName = stringResource(R.string.req_rsp_info_decompress_algorithm_desc),
                tint = Colors.inverseSurface,
                onSelectedChange = {
                    selectedDecompress = it
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppRoundCornerBox {
            val strNone = stringResource(R.string.req_rsp_info_none)
            val strText = stringResource(R.string.req_rsp_info_text)
            val strHtml = stringResource(R.string.req_rsp_info_html)
            val strImage = stringResource(R.string.req_rsp_info_image)
            AppMenuItem(
                icon = null,
                itemName = stringResource(R.string.req_rsp_info_formatter),
                selected = selectedFormatter,
                selectableList = remember { listOf(strNone, strText, strHtml, strImage) },
                subName = stringResource(R.string.req_rsp_info_formatter_desc),
                tint = Colors.inverseSurface,
                onSelectedChange = {
                    selectedFormatter = it
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        RealBox(modifier = Modifier.weight(1f)) {
            AppRoundCornerBox {
                RealBox {
                    LoadDetail(
                        sessionId = sessionId,
                        decompressTypeCode = selectedDecompress,
                        contentTypeCode = selectedFormatter
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun WireInfoUI.WireInfoRequestUIPage(
    request: HttpReq?
) {
    if (request == null) {
        RealBox { }
        return
    }
    RealBox {
        val headerList = remember {
            request.formatHead ?: return@remember emptyList()
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
        var httpBodyByteSize by remember { mutableIntStateOf(0) }
        LaunchedEffect(request.id) {
            withContext(Dispatchers.IO) {
                httpBodyByteSize = readHttpBytesById(request.id).httpBody().size
            }
        }
        LazyColumn {
            item {
                Spacer(modifier = Modifier.height(56.dp + statusBarHeightDp))
            }
            item {
                URLBox(request.url ?: "")
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                IPBox(request.sourcePort, request.destinationAddress, request.destinationPort)
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                AppRoundCornerBox {
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
                                title = stringResource(R.string.req_rsp_info_http_version),
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
                HeaderBox(
                    stringResource(R.string.request_info_headers),
                    headerList
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                BodySizeBox(byteSize = httpBodyByteSize)
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun WireInfoUI.WireInfoResponseUIPage(
    response: HttpRsp?
) {
    if (response == null) {
        RealBox { }
        return
    }
    RealBox {
        val headerList = remember {
            response.formatHead ?: return@remember emptyList()
            response.formatHead.subList(1, response.formatHead.size).map {
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
        var httpBodyByteSize by remember { mutableIntStateOf(0) }
        LaunchedEffect(response.id) {
            withContext(Dispatchers.IO) {
                httpBodyByteSize = readHttpBytesById(response.id).httpBody().size
            }
        }
        LazyColumn {
            item {
                Spacer(modifier = Modifier.height(56.dp + statusBarHeightDp))
            }
            item {
                URLBox(response.url ?: "")
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                IPBox(response.sourcePort, response.destinationAddress, response.destinationPort)
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                AppRoundCornerBox {
                    AppExpandableRichItem(
                        icon = R.drawable.ic_api,
                        title = stringResource(R.string.response_info_rsp_line),
                        expandable = false
                    ) {
                        RealColumn {
                            TextChapter(
                                title = stringResource(R.string.response_info_status_code),
                                content = response.rspStatus ?: ""
                            )
                            TextChapter(
                                title = stringResource(R.string.req_rsp_info_http_version),
                                content = response.httpVersion ?: "",
                                appendNewLine = false
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                if (response.formatHead == null || response.formatHead.size < 2) {
                    return@item
                }
                HeaderBox(
                    stringResource(R.string.response_info_headers),
                    headerList
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                BodySizeBox(byteSize = httpBodyByteSize)
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun URLBox(url: String) {
    var isUrlExpand by remember { mutableStateOf(false) }
    AppRoundCornerBox {
        AppExpandableTextItem(
            icon = R.drawable.ic_link,
            title = stringResource(R.string.req_rsp_info_url),
            body = url,
            expand = isUrlExpand,
            onLongClick = {
                copyTextToClipBoard(url)
                showToast(R.string.req_rsp_info_copy_success)
            }
        ) {
            isUrlExpand = !it
        }
    }
}

@Composable
fun IPBox(
    sourcePort: Short?,
    destinationAddress: String?,
    destinationPort: Short?
) {
    AppRoundCornerBox {
        val ipVersion = remember {
            WireBareHelper.parseIpVersion(destinationAddress)
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
            sourcePort?.toUShort()?.toString() ?: ""
        }
        val destIp = remember {
            destinationAddress?.toString() ?: ""
        }
        val destPort = remember {
            destinationPort?.toUShort()?.toString() ?: ""
        }
        AppExpandableRichItem(
            icon = R.drawable.ic_address,
            title = stringResource(R.string.request_info_ip),
            expandable = false
        ) {
            RealColumn {
                TextChapter(
                    title = stringResource(R.string.req_rsp_info_ip_version),
                    content = strIpVersion
                )
                TextChapter(
                    title = stringResource(R.string.req_rsp_info_ip_src),
                    content = "$srcIp:$srcPort"
                )
                TextChapter(
                    title = stringResource(R.string.req_rsp_info_ip_dest),
                    content = "$destIp:$destPort",
                    appendNewLine = false
                )
            }
        }
    }
}

@Composable
fun HeaderBox(
    title: String,
    headerList: List<Pair<String, String>>
) {
    var isOriginHeaderExpand by remember { mutableStateOf(false) }
    AppRoundCornerBox {
        val expandable = headerList.size > 4
        AppExpandableRichItem(
            icon = R.drawable.ic_wirebare,
            title = title,
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
}

@Composable
fun BodySizeBox(byteSize: Int) {
    var isUrlExpand by remember { mutableStateOf(false) }
    var byte by remember { mutableStateOf("") }
    var kByte by remember { mutableStateOf("") }
    var mByte by remember { mutableStateOf("") }
    LaunchedEffect(byteSize) {
        withContext(Dispatchers.IO) {
            val formatter = DecimalFormat(
                "#0.##",
                DecimalFormatSymbols(Locale.US)
            )
            byte = "$byteSize B"
            kByte = "${formatter.format(byteSize / 1024.0)} KB"
            mByte = "${formatter.format(byteSize / 1024.0 / 1024.0)} MB"
        }
    }
    AppRoundCornerBox {
        AppExpandableTextItem(
            icon = R.drawable.ic_wirebare,
            title = stringResource(R.string.req_rsp_info_body_size),
            body = "$byte = $kByte = $mByte",
            expand = isUrlExpand
        ) {
            isUrlExpand = !it
        }
    }
}

@Composable
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
                R.string.req_rsp_info_content_too_long
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
