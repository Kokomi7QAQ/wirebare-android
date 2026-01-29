package top.sankokomi.wirebare.ui.launcher

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.sankokomi.wirebare.kernel.interceptor.http.HttpHeaderParser
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.record.HttpRecorder
import top.sankokomi.wirebare.ui.record.HttpReq
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.TextTag
import top.sankokomi.wirebare.ui.wireinfo.WireInfoUI

@Composable
fun LauncherUI.PageProxyRequestResult(requestList: SnapshotStateList<HttpReq>) {
    PageProxyResultList(
        emptyText = stringResource(R.string.request_list_empty),
        onClear = { HttpRecorder.clearReqRecord() },
        resultList = requestList,
        sourceProcessUid = { it.sourceProcessUid },
        onClick = { request ->
            startActivity(
                Intent(
                    this@PageProxyRequestResult,
                    WireInfoUI::class.java
                ).also {
                    it.putExtra("request", request)
                }
            )
        },
        url = { it.url ?: it.destinationAddress },
        headText = {
            if (HttpHeaderParser.isHttpVersion(it.httpVersion)) {
                it.formatHead?.firstOrNull()
            } else {
                null
            }
        },
    ) {
        TextTag(
            text = if (it.isHttps == true) stringResource(R.string.common_ssl) else null,
            borderColor = Colors.primaryContainer,
            corner = 6.dp,
            space = 8.dp
        )
        val showVersion = remember(it) { HttpHeaderParser.isHttpVersion(it.httpVersion) }
        if (showVersion) {
            TextTag(
                text = it.method,
                borderColor = Colors.primaryContainer,
                corner = 6.dp,
                space = 8.dp
            )
            TextTag(
                text = it.httpVersion,
                borderColor = Colors.primaryContainer,
                corner = 6.dp,
                space = 0.dp
            )
        }
    }
}