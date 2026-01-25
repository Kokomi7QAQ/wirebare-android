package top.sankokomi.wirebare.ui.launcher

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.record.HttpRecorder
import top.sankokomi.wirebare.ui.record.HttpRsp
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.TextTag
import top.sankokomi.wirebare.ui.wireinfo.WireInfoUI

@Composable
fun LauncherUI.PageProxyResponseResult(responseList: SnapshotStateList<HttpRsp>) {
    PageProxyResultList(
        emptyText = stringResource(R.string.response_list_empty),
        onClear = { HttpRecorder.clearReqRecord() },
        resultList = responseList,
        sourceProcessUid = { it.sourceProcessUid },
        onClick = { response ->
            startActivity(
                Intent(
                    this@PageProxyResponseResult,
                    WireInfoUI::class.java
                ).also {
                    it.putExtra("response", response)
                }
            )
        },
        url = { it.url ?: it.destinationAddress },
        headText = { it.formatHead?.firstOrNull() },
    ) {
        TextTag(
            text = if (it.isHttps == true) stringResource(R.string.common_ssl) else null,
            borderColor = Colors.primaryContainer,
            corner = 6.dp,
            space = 8.dp
        )
        TextTag(
            text = it.rspStatus,
            borderColor = Colors.primaryContainer,
            corner = 6.dp,
            space = 8.dp
        )
        TextTag(
            text = it.contentEncoding,
            borderColor = Colors.primaryContainer,
            corner = 6.dp,
            space = 8.dp
        )
        TextTag(
            text = it.contentType,
            borderColor = Colors.primaryContainer,
            corner = 6.dp,
            space = 0.dp
        )
    }
}
