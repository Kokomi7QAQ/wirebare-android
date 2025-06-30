package top.sankokomi.wirebare.ui.launcher

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.record.HttpRecorder
import top.sankokomi.wirebare.ui.record.HttpReq
import top.sankokomi.wirebare.ui.resources.ImageButton
import top.sankokomi.wirebare.ui.resources.LightGreen
import top.sankokomi.wirebare.ui.resources.LightGrey
import top.sankokomi.wirebare.ui.resources.MediumGreen
import top.sankokomi.wirebare.ui.resources.MediumGrey
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.RealRow
import top.sankokomi.wirebare.ui.resources.Tag
import top.sankokomi.wirebare.ui.util.injectTouchEffect
import top.sankokomi.wirebare.ui.wireinfo.WireInfoUI

@Composable
fun LauncherUI.PageProxyRequestResult(requestList: SnapshotStateList<HttpReq>) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(requestList.size) { i ->
                val index = requestList.size - i - 1
                val request = requestList[index]
                val itemShape = if (requestList.size == 1) {
                    RoundedCornerShape(size = 24.dp)
                } else {
                    when (i) {
                        0 -> {
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        }

                        requestList.size - 1 -> {
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
                            HorizontalDivider(
                                modifier = Modifier
                                    .background(Color.White)
                                    .padding(start = 16.dp, end = 16.dp)
                                    .fillMaxWidth()
                                    .height(0.2.dp)
                                    .background(LightGrey)
                            )
                        }
                        RealRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .injectTouchEffect(normalBackground = Color.White) {
                                    startActivity(
                                        Intent(
                                            this@PageProxyRequestResult,
                                            WireInfoUI::class.java
                                        ).apply {
                                            putExtra("request", request)
                                            putExtra("session_id", request.id)
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
                                    text = request.url ?: request.destinationAddress
                                    ?: "[格式化 URL 失败]",
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = request.formatHead?.getOrNull(0) ?: "[格式化请求头失败]",
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MediumGrey,
                                    fontSize = 12.sp,
                                    lineHeight = 12.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                RealRow {
                                    Tag(
                                        borderColor = MediumGreen,
                                        corner = 6.dp
                                    ) {
                                        Text(
                                            text = request.method ?: "",
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(LightGreen)
                                                .padding(horizontal = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Tag(
                                        borderColor = MediumGreen,
                                        corner = 6.dp
                                    ) {
                                        Text(
                                            text = request.httpVersion ?: "",
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(LightGreen)
                                                .padding(horizontal = 4.dp)
                                        )
                                    }
                                    if (request.isHttps == true) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Tag(
                                            borderColor = MediumGreen,
                                            corner = 6.dp
                                        ) {
                                            Text(
                                                text = "HTTPS",
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(LightGreen)
                                                    .padding(horizontal = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
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
                        HttpRecorder.clearReqRecord()
                    }
                    requestList.clear()
                }
            }
        }
    }
}