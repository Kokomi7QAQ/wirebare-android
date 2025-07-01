package top.sankokomi.wirebare.ui.launcher

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import top.sankokomi.wirebare.kernel.common.EventSynopsis
import top.sankokomi.wirebare.kernel.common.ProxyStatus
import top.sankokomi.wirebare.kernel.common.WireBare
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.accesscontrol.AccessControlUI
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.record.HttpReq
import top.sankokomi.wirebare.ui.record.HttpRsp
import top.sankokomi.wirebare.ui.resources.AppTitleBar
import top.sankokomi.wirebare.ui.resources.CornerSlideBar
import top.sankokomi.wirebare.ui.resources.LGreenC
import top.sankokomi.wirebare.ui.resources.RedZ
import top.sankokomi.wirebare.ui.resources.LGreenA
import top.sankokomi.wirebare.ui.resources.LGrayA
import top.sankokomi.wirebare.ui.resources.LargeColorfulText
import top.sankokomi.wirebare.ui.resources.LGrayC
import top.sankokomi.wirebare.ui.resources.RealBox
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.RealRow
import kotlin.math.roundToInt

@Composable
fun LauncherUI.WireBareUIPage() {
    val pagerState = rememberPagerState { 3 }
    val painterControlRes = painterResource(R.drawable.ic_wirebare)
    val painterRequestRes = painterResource(R.drawable.ic_request)
    val painterResponseRes = painterResource(R.drawable.ic_response)
    val isBanFilter by ProxyPolicyDataStore.banAutoFilter.collectAsState()
    val requestList = remember { mutableStateListOf<HttpReq>() }
    LaunchedEffect(Unit) {
        requestFlow.collect {
            if (!isBanFilter) {
                if (it.url == null) return@collect
//                if (it.httpVersion?.startsWith("HTTP") != true) return@collect
            }
            requestList.add(it)
        }
    }
    val responseList = remember { mutableStateListOf<HttpRsp>() }
    LaunchedEffect(Unit) {
        responseFlow.collect {
            if (!isBanFilter) {
                if (it.url == null) return@collect
//                if (it.httpVersion?.startsWith("HTTP") != true) return@collect
            }
            responseList.add(it)
        }
    }
    queryRecord()
    val anim = remember { Animatable(1f) }
    RealColumn(modifier = Modifier.background(LGrayA)) {
        AppTitleBar()
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            beyondViewportPageCount = 3,
            modifier = Modifier
                .weight(1f)
                .padding(top = ((1f - anim.value) * 4).dp)
                .alpha(1f - ((1f - anim.value)))
        ) {
            when (it) {
                0 -> PageControlCenter()
                1 -> PageProxyRequestResult(requestList)
                2 -> PageProxyResponseResult(responseList)
            }
        }
        val navigationItems = listOf(
            (painterControlRes to "控制中心") to (painterControlRes to "控制中心"),
            (painterRequestRes to "  请求  ") to (painterRequestRes to "  请求  "),
            (painterResponseRes to "  响应  ") to (painterResponseRes to "  响应  ")
        )
        val rememberScope = rememberCoroutineScope()
        RealRow(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp)
                .background(LGreenA)
                .padding(vertical = 2.dp)
        ) {
            for (index in navigationItems.indices) {
                val item = navigationItems[index]
                RealBox(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1F)
                ) {
                    val (painter, _) = if (pagerState.currentPage != index) {
                        item.first
                    } else {
                        item.second
                    }
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                if (pagerState.currentPage != index) {
                                    rememberScope.launch {
                                        anim.stop()
                                        anim.animateTo(0f, tween(100))
                                        pagerState.animateScrollToPage(
                                            index,
                                            animationSpec = tween(0)
                                        )
                                        anim.animateTo(1f, tween(100))
                                    }
                                }
                            }
                    ) {
                        Image(
                            painter = painter,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(32.dp),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LauncherUI.PageControlCenter() {
    var wireBareStatus by remember { mutableStateOf(ProxyStatus.DEAD) }
    val isBanFilter by ProxyPolicyDataStore.banAutoFilter.collectAsState()
    val enableIpv6 by ProxyPolicyDataStore.enableIpv6.collectAsState()
    val enableSSL by ProxyPolicyDataStore.enableSSL.collectAsState()
    val mockPacketLossProbability by ProxyPolicyDataStore.mockPacketLossProbability.collectAsState()
    var maybeUnsupportedIpv6 by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        proxyStatusFlow.collect {
            maybeUnsupportedIpv6 = false
            wireBareStatus = it
        }
    }
    LaunchedEffect(Unit) {
        eventFlow.collect { event ->
            when (event.synopsis) {
                EventSynopsis.IPV6_UNREACHABLE -> {
                    maybeUnsupportedIpv6 = true
                }

                else -> {}
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(6.dp))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                AnimatedContent(
                    targetState = wireBareStatus,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    label = "WireBareStatus"
                ) { status ->
                    val mainText: String
                    val subText: String
                    val backgroundColor: Color
                    val textColor: Color
                    val onClick: () -> Unit
                    when (status) {
                        ProxyStatus.DEAD -> {
                            mainText = "已停止"
                            subText = "点此启动"
                            backgroundColor = LGreenC
                            textColor = Color.White
                            onClick = ::startProxy
                        }

                        ProxyStatus.STARTING -> {
                            mainText = "正在启动"
                            subText = "请稍后"
                            backgroundColor = LGrayC
                            textColor = Color.White
                            onClick = ::stopProxy
                        }

                        ProxyStatus.ACTIVE -> {
                            mainText = "已启动"
                            subText = "点此停止"
                            backgroundColor = LGreenA
                            textColor = Color.Black
                            onClick = ::stopProxy
                        }

                        ProxyStatus.DYING -> {
                            mainText = "正在停止"
                            subText = "请稍后"
                            backgroundColor = LGrayC
                            textColor = Color.White
                            onClick = ::stopProxy
                        }
                    }
                    LargeColorfulText(
                        mainText = mainText,
                        subText = subText,
                        backgroundColor = backgroundColor,
                        textColor = textColor,
                        onClick = onClick
                    )
                }

            }
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedVisibility(
                visible = wireBareStatus == ProxyStatus.ACTIVE || wireBareStatus == ProxyStatus.STARTING
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp),
                    text = "下面的配置修改后需要重启服务生效",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = "访问控制",
                    subText = "配置代理应用",
                    backgroundColor = LGreenA,
                    textColor = Color.Black,
                    onClick = {
                        startActivity(
                            Intent(
                                this@PageControlCenter,
                                AccessControlUI::class.java
                            )
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            val afMainText: String
            val afSubText: String
            val afBackgroundColor: Color
            val afTextColor: Color
            if (isBanFilter) {
                afMainText = "自动过滤已停用"
                afSubText = "将显示代理到的所有请求"
                afBackgroundColor = LGreenC
                afTextColor = Color.White
            } else {
                afMainText = "自动过滤已启用"
                afSubText = "将会自动过滤无法解析的请求"
                afBackgroundColor = LGreenA
                afTextColor = Color.Black
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                LargeColorfulText(
                    mainText = afMainText,
                    subText = afSubText,
                    backgroundColor = afBackgroundColor,
                    textColor = afTextColor,
                    onClick = {
                        ProxyPolicyDataStore.banAutoFilter.value = !isBanFilter
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                AnimatedContent(
                    targetState = enableSSL,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    label = "SslTlsStatus"
                ) { enable ->
                    val sslMainText: String
                    val sslSubText: String
                    val sslBackgroundColor: Color
                    val sslTextColor: Color
                    if (enable) {
                        sslMainText = "SSL/TLS 已启用"
                        sslSubText = "进行 SSL/TLS 握手并解密 HTTPS"
                        sslBackgroundColor = LGreenA
                        sslTextColor = Color.Black
                    } else {
                        sslMainText = "SSL/TLS 已禁用"
                        sslSubText = "仅对 HTTPS 透明代理"
                        sslBackgroundColor = LGreenC
                        sslTextColor = Color.White
                    }
                    LargeColorfulText(
                        mainText = sslMainText,
                        subText = sslSubText,
                        backgroundColor = sslBackgroundColor,
                        textColor = sslTextColor,
                        onClick = {
                            ProxyPolicyDataStore.enableSSL.value = !enableSSL
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedVisibility(
                visible = enableSSL
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp),
                    text = "可能需要您事先安装好代理服务器根证书",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                AnimatedContent(
                    targetState = enableIpv6,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    label = "Ipv6Status"
                ) { enable ->
                    val i6MainText: String
                    val i6SubText: String
                    val i6BackgroundColor: Color
                    val i6TextColor: Color
                    if (enable) {
                        i6MainText = "IPv6 代理已启用"
                        i6SubText = "代理 IPv4 和 IPv6 数据包"
                        i6BackgroundColor = LGreenA
                        i6TextColor = Color.Black
                    } else {
                        i6MainText = "IPv6 代理已禁用"
                        i6SubText = "仅代理 IPv4 数据包"
                        i6BackgroundColor = LGreenC
                        i6TextColor = Color.White
                    }
                    LargeColorfulText(
                        mainText = i6MainText,
                        subText = i6SubText,
                        backgroundColor = i6BackgroundColor,
                        textColor = i6TextColor,
                        onClick = {
                            ProxyPolicyDataStore.enableIpv6.value = !enableIpv6
                        }
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
            ) {
                CornerSlideBar(
                    mainText = "随机丢包概率",
                    subText = "调整后立即生效",
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black,
                    barColor = LGreenC,
                    barBackgroundColor = LGreenA,
                    value = mockPacketLossProbability / 100f,
                    valueRange = 0f..1f,
                    onValueChange = {
                        val probability = (it * 100).roundToInt()
                        ProxyPolicyDataStore.mockPacketLossProbability.value = probability
                        WireBare.dynamicConfiguration.mockPacketLossProbability = probability
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(
                visible = maybeUnsupportedIpv6 && wireBareStatus == ProxyStatus.ACTIVE
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    LargeColorfulText(
                        mainText = "注意",
                        subText = "当前网络疑似不支持 IPv6",
                        backgroundColor = RedZ,
                        textColor = Color.White,
                        onClick = {
                            ProxyPolicyDataStore.enableIpv6.value = !enableIpv6
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
