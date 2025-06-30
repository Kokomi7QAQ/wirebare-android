package top.sankokomi.wirebare.ui.launcher

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.record.HttpRecorder
import top.sankokomi.wirebare.ui.record.HttpRsp
import top.sankokomi.wirebare.ui.resources.ImageButton
import top.sankokomi.wirebare.ui.resources.Purple80
import top.sankokomi.wirebare.ui.resources.SmallColorfulText
import top.sankokomi.wirebare.ui.wireinfo.WireInfoUI

@Composable
fun LauncherUI.PageProxyResponseResult(responseList: SnapshotStateList<HttpRsp>) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            items(responseList.size) { i ->
                val index = responseList.size - i - 1
                val response = responseList[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            startActivity(
                                Intent(
                                    this@PageProxyResponseResult,
                                    WireInfoUI::class.java
                                ).apply {
                                    putExtra("response", response)
                                    putExtra("session_id", response.id)
                                }
                            )
                        }
                ) {
                    SmallColorfulText(
                        mainText = response.url ?: response.destinationAddress ?: "",
                        subText = (response.formatHead?.getOrNull(0) ?: "") +
                                System.lineSeparator() +
                                (response.contentType ?: "") +
                                System.lineSeparator() +
                                (response.contentEncoding ?: "identity"),
                        backgroundColor = Purple80,
                        textColor = Color.Black
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        Box(
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            val rememberScope = rememberCoroutineScope()
            ImageButton(
                painter = painterResource(id = R.drawable.ic_clear)
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
