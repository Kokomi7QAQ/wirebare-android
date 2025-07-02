package top.sankokomi.wirebare.ui.launcher

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import top.sankokomi.wirebare.kernel.common.EventSynopsis
import top.sankokomi.wirebare.kernel.common.ProxyStatus
import top.sankokomi.wirebare.kernel.common.WireBareHelper
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.record.HttpReq
import top.sankokomi.wirebare.ui.record.HttpRsp
import top.sankokomi.wirebare.ui.resources.AppCheckableItem
import top.sankokomi.wirebare.ui.resources.AppRoundCornerBox
import top.sankokomi.wirebare.ui.resources.AppTab
import top.sankokomi.wirebare.ui.resources.AppTitleBar
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.LGrayA
import top.sankokomi.wirebare.ui.resources.RealBox
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.TabData
import top.sankokomi.wirebare.ui.util.statusBarHeightDp

@Composable
fun LauncherUI.WireBareUIPage() {
    val tabDataList = remember {
        listOf(
            TabData(R.drawable.ic_wirebare, "控制中心"),
            TabData(R.drawable.ic_access_control, "访问控制"),
            TabData(R.drawable.ic_request, "请求"),
            TabData(R.drawable.ic_response, "响应")
        )
    }
    val pagerState = rememberPagerState { tabDataList.size }
    val titleBarText = remember { mutableStateOf("控制中心") }
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
                0 -> PageControlCenter()
                1 -> AccessControlPage()
                2 -> PageProxyRequestResult(requestList)
                3 -> PageProxyResponseResult(responseList)
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
private fun LauncherUI.PageControlCenter() {
    var wireBareStatus by remember { mutableStateOf(ProxyStatus.DEAD) }
//    val mockPacketLossProbability by ProxyPolicyDataStore.mockPacketLossProbability.collectAsState()
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
    RealColumn(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .clip(RoundedCornerShape(6.dp))
    ) {
        Spacer(modifier = Modifier.height(statusBarHeightDp + 56.dp))
        ControlBox(wireBareStatus, maybeUnsupportedIpv6)
        Spacer(modifier = Modifier.height(80.dp))
//        Box(
//            modifier = Modifier
//                .padding(16.dp)
//                .clip(RoundedCornerShape(6.dp))
//        ) {
//            CornerSlideBar(
//                mainText = "随机丢包概率",
//                subText = "调整后立即生效",
//                backgroundColor = Color.Transparent,
//                textColor = Color.Black,
//                barColor = LGreenC,
//                barBackgroundColor = LGreenA,
//                value = mockPacketLossProbability / 100f,
//                valueRange = 0f..1f,
//                onValueChange = {
//                    val probability = (it * 100).roundToInt()
//                    ProxyPolicyDataStore.mockPacketLossProbability.value = probability
//                    WireBare.dynamicConfiguration.mockPacketLossProbability = probability
//                }
//            )
//        }
    }
}

@Composable
private fun LauncherUI.ControlBox(
    status: ProxyStatus,
    maybeUnsupportedIpv6: Boolean
) {
    val tint = Colors.primary
    val switchChecked = remember { mutableStateOf(false) }
    var switchEnabled = remember { mutableStateOf(true) }
    val switchSubName = remember { mutableStateOf("已终止，点击以开始") }
    val banFilter = ProxyPolicyDataStore.banAutoFilter.collectAsState()
    val autoFilterChecked = !banFilter.value
    var isWireBareActive = remember { mutableStateOf(false) }
    val enableSSL = ProxyPolicyDataStore.enableSSL.collectAsState()
    val systemTrustCert = remember(enableSSL.value) {
        WireBareHelper.checkSystemTrustCert("318facc2.0")
    }
    val enableSSLSubName = remember(enableSSL.value, systemTrustCert) {
        if (!enableSSL.value) {
            "启用后会解析 HTTPS 请求/响应"
        } else {
            if (!systemTrustCert) {
                "检测到系统未信任代理服务器证书"
            } else {
                "已启用 HTTPS 解析"
            }
        }
    }
    val enableIpv6 = ProxyPolicyDataStore.enableIpv6.collectAsState()
    val enableIpv6SubName = remember(enableIpv6.value, maybeUnsupportedIpv6) {
        if (maybeUnsupportedIpv6) {
            "当前网络疑似不支持 IPv6"
        } else {
            if (!enableIpv6.value) {
                "仅代理 IPv4 流量"
            } else {
                "代理 IPv4 和 IPv6 流量"
            }
        }
    }
    LaunchedEffect(status.ordinal) {
        switchChecked.value = status == ProxyStatus.STARTING || status == ProxyStatus.ACTIVE
        switchEnabled.value = status == ProxyStatus.ACTIVE || status == ProxyStatus.DEAD
        switchSubName.value = when (status) {
            ProxyStatus.STARTING -> "正在启动"
            ProxyStatus.DYING -> "正在终止"
            ProxyStatus.DEAD -> "已终止，点击以开始"
            ProxyStatus.ACTIVE -> "已启动，点击以终止"
        }
        isWireBareActive.value = status != ProxyStatus.DEAD
    }
    RealColumn {
        AppRoundCornerBox {
            RealColumn {
                AppCheckableItem(
                    itemName = "总开关",
                    checked = switchChecked.value,
                    icon = R.drawable.ic_wirebare,
                    subName = switchSubName.value,
                    enabled = switchEnabled.value,
                    tint = tint
                ) {
                    if (it) startProxy() else stopProxy()
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppRoundCornerBox {
            RealColumn {
                AppCheckableItem(
                    itemName = "自动过滤",
                    checked = autoFilterChecked,
                    icon = R.drawable.ic_filter,
                    subName = "启用后会跳过无法解析的请求/响应",
                    enabled = !isWireBareActive.value,
                    tint = tint
                ) {
                    ProxyPolicyDataStore.banAutoFilter.value =
                        !ProxyPolicyDataStore.banAutoFilter.value
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppRoundCornerBox {
            RealColumn {
                AppCheckableItem(
                    itemName = "SSL/TLS",
                    checked = enableSSL.value,
                    icon = R.drawable.ic_cert,
                    subName = enableSSLSubName,
                    isWarning = enableSSL.value && !systemTrustCert,
                    enabled = !isWireBareActive.value,
                    tint = tint
                ) {
                    ProxyPolicyDataStore.enableSSL.value = !ProxyPolicyDataStore.enableSSL.value
                }
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 48.dp, end = 16.dp)
                        .fillMaxWidth()
                        .height(0.2.dp)
                        .background(LGrayA)
                )
                AppCheckableItem(
                    itemName = "IPv6代理",
                    checked = enableIpv6.value,
                    icon = R.drawable.ic_ipv6,
                    subName = enableIpv6SubName,
                    isWarning = maybeUnsupportedIpv6,
                    enabled = !isWireBareActive.value,
                    tint = tint
                ) {
                    ProxyPolicyDataStore.enableIpv6.value = !ProxyPolicyDataStore.enableIpv6.value
                }
            }
        }
    }
}
