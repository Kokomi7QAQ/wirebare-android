package top.sankokomi.wirebare.ui.knet

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.sankokomi.wirebare.kernel.common.BandwidthLimiter
import top.sankokomi.wirebare.kernel.common.ProxyStatus
import top.sankokomi.wirebare.kernel.common.WireBare
import top.sankokomi.wirebare.kernel.dashboard.WireBareDashboard
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.datastore.KnetPolicyDataStore
import top.sankokomi.wirebare.ui.launcher.LauncherModel
import top.sankokomi.wirebare.ui.launcher.LauncherUI
import top.sankokomi.wirebare.ui.resources.AppCheckableItem
import top.sankokomi.wirebare.ui.resources.AppRoundCornerBox
import top.sankokomi.wirebare.ui.resources.CornerDampingSlideBar
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.util.Global
import top.sankokomi.wirebare.ui.util.showToast
import top.sankokomi.wirebare.ui.util.statusBarHeightDp


@Composable
fun LauncherUI.KnetPage() {
    var wireBareStatus by remember { mutableStateOf(ProxyStatus.DEAD) }
    LaunchedEffect(Unit) {
        proxyStatusFlow.collect {
            wireBareStatus = it
        }
    }
    RealColumn(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .clip(RoundedCornerShape(6.dp))
    ) {
        Spacer(modifier = Modifier.height(statusBarHeightDp + 56.dp))
        KnetBox(wireBareStatus)
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
private fun LauncherUI.KnetBox(
    status: ProxyStatus
) {
    val switchChecked = remember { mutableStateOf(false) }
    val switchEnabled = remember { mutableStateOf(true) }
    val strSwitchDead = stringResource(R.string.control_center_main_switch_dead)
    val strSwitchLaunching = stringResource(R.string.control_center_main_switch_launching)
    val strSwitchActive = stringResource(R.string.control_center_main_switch_active)
    val strSwitchDying = stringResource(R.string.control_center_main_switch_dying)
    val switchSubName = remember { mutableStateOf(strSwitchDead) }
    val isWireBareActive = remember { mutableStateOf(false) }
    val enableFloatingWindow by KnetPolicyDataStore.enableFloatingWindow.collectAsState()
    val enableFloatingWindowOnlyInBackground by KnetPolicyDataStore.enableFloatingWindowOnlyInBackground.collectAsState()
    val enableMiniFloatingWindow by KnetPolicyDataStore.enableMiniFloatingWindow.collectAsState()
    val unitByte = remember { BandwidthUnit.B.symbol }
    val unitKByte = remember { BandwidthUnit.KB.symbol }
    val unitMByte = remember { BandwidthUnit.MB.symbol }
    val unitSecond = stringResource(R.string.unit_second)
    val maxBandwidthLimit = remember { 2L * LauncherModel.MTU }
    val strReqBandwidthLimit = stringResource(R.string.knet_req_bandwidth_limit)
    val strRspBandwidthLimit = stringResource(R.string.knet_rsp_bandwidth_limit)
    val strBandwidthNoLimit = stringResource(R.string.knet_bandwidth_no_limit)
    val reqBandwidthLimit by KnetPolicyDataStore.reqBandwidthLimit.collectAsState()
    val rspBandwidthLimit by KnetPolicyDataStore.rspBandwidthLimit.collectAsState()
    LaunchedEffect(status.ordinal) {
        switchChecked.value = status == ProxyStatus.STARTING || status == ProxyStatus.ACTIVE
        switchEnabled.value = status == ProxyStatus.ACTIVE || status == ProxyStatus.DEAD
        switchSubName.value = when (status) {
            ProxyStatus.STARTING -> strSwitchLaunching
            ProxyStatus.DYING -> strSwitchDying
            ProxyStatus.DEAD -> strSwitchDead
            ProxyStatus.ACTIVE -> strSwitchActive
        }
        isWireBareActive.value = status != ProxyStatus.DEAD
    }
    RealColumn {
        AppRoundCornerBox {
            RealColumn {
                AppCheckableItem(
                    itemName = stringResource(R.string.control_center_main_switch),
                    checked = switchChecked.value,
                    icon = R.drawable.ic_wirebare,
                    subName = switchSubName.value,
                    enabled = switchEnabled.value
                ) {
                    if (it) startProxy() else stopProxy()
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppRoundCornerBox {
            RealColumn {
                AppCheckableItem(
                    itemName = stringResource(R.string.knet_floating_window),
                    checked = enableFloatingWindow,
                    icon = R.drawable.ic_floating_window
                ) {
                    if (it && !Settings.canDrawOverlays(Global.appContext)) {
                        showToast(Global.appContext.getString(R.string.permission_draw_overlays))
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${Global.appContext.packageName}")
                        )
                        startActivity(intent)
                        return@AppCheckableItem
                    }
                    KnetPolicyDataStore.enableFloatingWindow.value = it
                }
                AnimatedVisibility(enableFloatingWindow) {
                    RealColumn {
                        AppCheckableItem(
                            itemName = stringResource(R.string.knet_floating_window_background),
                            checked = enableFloatingWindowOnlyInBackground,
                            icon = R.drawable.ic_background
                        ) {
                            KnetPolicyDataStore.enableFloatingWindowOnlyInBackground.value = it
                        }
                        AppCheckableItem(
                            itemName = stringResource(R.string.knet_mini_floating_window),
                            subName = stringResource(R.string.knet_mini_floating_window_desc),
                            checked = enableMiniFloatingWindow,
                            icon = R.drawable.ic_mini_window
                        ) {
                            KnetPolicyDataStore.enableMiniFloatingWindow.value = it
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppRoundCornerBox {
            CornerDampingSlideBar(
                icon = R.drawable.ic_request,
                itemName = strReqBandwidthLimit,
                damping = 3.0f,
                max = maxBandwidthLimit,
                value = reqBandwidthLimit,
                onValueChange = {
                    KnetPolicyDataStore.reqBandwidthLimit.value = it
                    WireBare.dynamicConfig.reqBandwidthLimiter = BandwidthLimiter(max = it)
                },
                valueText = {
                    if (it !in 1..<maxBandwidthLimit) {
                        strBandwidthNoLimit
                    } else {
                        "$it $unitKByte/$unitSecond"
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppRoundCornerBox {
            CornerDampingSlideBar(
                icon = R.drawable.ic_response,
                itemName = strRspBandwidthLimit,
                damping = 3.0f,
                max = maxBandwidthLimit,
                value = rspBandwidthLimit,
                onValueChange = {
                    KnetPolicyDataStore.rspBandwidthLimit.value = it
                    WireBare.dynamicConfig.rspBandwidthLimiter = BandwidthLimiter(max = it)
                },
                valueText = {
                    if (it !in 1..<maxBandwidthLimit) {
                        strBandwidthNoLimit
                    } else {
                        "$it $unitKByte/$unitSecond"
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppRoundCornerBox {
            BandwidthChart(
                icon = R.drawable.ic_bandwidth,
                itemName = stringResource(R.string.knet_bandwidth),
                subName = "",
                WireBareDashboard.bandwidthFlow
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppRoundCornerBox {
            BandwidthChart(
                icon = R.drawable.ic_request,
                itemName = stringResource(R.string.knet_req_bandwidth),
                subName = "",
                WireBareDashboard.reqBandwidthFlow
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppRoundCornerBox {
            BandwidthChart(
                icon = R.drawable.ic_response,
                itemName = stringResource(R.string.knet_rsp_bandwidth),
                subName = "",
                WireBareDashboard.rspBandwidthFlow
            )
        }
    }
}
