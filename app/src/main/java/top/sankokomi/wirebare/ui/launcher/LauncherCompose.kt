package top.sankokomi.wirebare.ui.launcher

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.knet.KnetPage
import top.sankokomi.wirebare.ui.record.HttpReq
import top.sankokomi.wirebare.ui.record.HttpRsp
import top.sankokomi.wirebare.ui.resources.AppTab
import top.sankokomi.wirebare.ui.resources.AppTitleBar
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.RealBox
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.StatusBarSpacer
import top.sankokomi.wirebare.ui.resources.TabData

@Composable
fun LauncherUI.WireBareUIPage() {
    val strAccessControl = stringResource(R.string.access_control_title)
    val strKnet = stringResource(R.string.knet_title)
    val strControlCenter = stringResource(R.string.control_center_title)
    val strRequestList = stringResource(R.string.request_list_title)
    val strResponseList = stringResource(R.string.response_list_title)
    val tabDataList = remember {
        listOf(
            TabData(R.drawable.ic_access_control, strAccessControl),
            TabData(R.drawable.ic_knet, strKnet),
            TabData(R.drawable.ic_wirebare, strControlCenter),
            TabData(R.drawable.ic_request, strRequestList),
            TabData(R.drawable.ic_response, strResponseList)
        )
    }
    val initialPage = remember { ProxyPolicyDataStore.mainTabIndex.value }
    val pagerState = rememberPagerState(initialPage = initialPage) { tabDataList.size }
    val titleBarText = remember { mutableStateOf(tabDataList[initialPage].text) }
    val isBanFilter by ProxyPolicyDataStore.banAutoFilter.collectAsState()
    val requestList = remember { mutableStateListOf<HttpReq>() }
    LaunchedEffect(Unit) {
        requestFlow.collect {
            if (!isBanFilter) {
                if (it.url == null) return@collect
            }
            requestList.add(it)
        }
    }
    val responseList = remember { mutableStateListOf<HttpRsp>() }
    LaunchedEffect(Unit) {
        responseFlow.collect {
            if (!isBanFilter) {
                if (it.url == null) return@collect
            }
            responseList.add(it)
        }
    }
    LaunchedEffect(Unit) {
        queryRecord()
    }
    val anim = remember { Animatable(1f) }
    RealBox(modifier = Modifier.background(Colors.background)) {
        RealColumn(
            modifier = Modifier.zIndex(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Colors.background)
            ) {
                StatusBarSpacer()
            }
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
                0 -> AccessControlPage()
                1 -> KnetPage()
                2 -> PageControlCenter()
                3 -> PageProxyRequestResult(requestList)
                4 -> PageProxyResponseResult(responseList)
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
                    ProxyPolicyDataStore.mainTabIndex.value = index
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
