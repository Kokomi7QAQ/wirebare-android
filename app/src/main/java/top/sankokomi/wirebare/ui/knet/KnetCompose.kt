package top.sankokomi.wirebare.ui.knet

import android.content.Intent
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
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.launcher.LauncherUI
import top.sankokomi.wirebare.ui.resources.AppCheckableItem
import top.sankokomi.wirebare.ui.resources.AppRoundCornerBox
import top.sankokomi.wirebare.ui.resources.CornerSlideBar
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.util.statusBarHeightDp
import kotlin.math.pow

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
    val enableFloatingWindow by ProxyPolicyDataStore.enableFloatingWindow.collectAsState()
    val strReqBandwidthLimit = stringResource(R.string.knet_req_bandwidth_limit)
    val strRspBandwidthLimit = stringResource(R.string.knet_rsp_bandwidth_limit)
    val strBandwidthNoLimit = stringResource(R.string.knet_bandwidth_no_limit)
    val reqBandwidthLimit by ProxyPolicyDataStore.reqBandwidthLimit.collectAsState()
    val rspBandwidthLimit by ProxyPolicyDataStore.rspBandwidthLimit.collectAsState()
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
                    icon = R.drawable.ic_floating_window,
                    subName = ""
                ) {
                    ProxyPolicyDataStore.enableFloatingWindow.value = it
                    if (it) {
                        val intent = Intent(KnetFloatingDashboardService.ACTION_SHOW).apply {
                            `package` = packageName
                        }
                        startService(intent)
                    } else {
                        val intent = Intent(KnetFloatingDashboardService.ACTION_HIDE).apply {
                            `package` = packageName
                        }
                        stopService(intent)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppRoundCornerBox {
            CornerSlideBar(
                itemName = strReqBandwidthLimit,
                subName = strReqBandwidthLimit,
                icon = R.drawable.ic_request,
                value = reqBandwidthLimit.toPercent(16384L),
                valueRange = 0f..1f,
                onValueChange = {
                    val max = it.toValue(16384L)
                    ProxyPolicyDataStore.reqBandwidthLimit.value = max
                    WireBare.dynamicConfig.reqBandwidthLimiter = BandwidthLimiter(max = max)
                },
                valueText = {
                    if (it <= 0f) {
                        strBandwidthNoLimit
                    } else {
                        "${it.toValue(16384L)} KB/s"
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppRoundCornerBox {
            CornerSlideBar(
                itemName = strRspBandwidthLimit,
                subName = strRspBandwidthLimit,
                icon = R.drawable.ic_response,
                value = rspBandwidthLimit.toPercent(16384L),
                valueRange = 0f..1f,
                onValueChange = {
                    val max = it.toValue(16384L)
                    ProxyPolicyDataStore.rspBandwidthLimit.value = max
                    WireBare.dynamicConfig.rspBandwidthLimiter = BandwidthLimiter(max = max)
                },
                valueText = {
                    if (it <= 0f) {
                        strBandwidthNoLimit
                    } else {
                        "${it.toValue(16384L)} KB/s"
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        RealColumn {
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
}

private fun Float.toValue(max: Long): Long {
    return (max * coerceIn(0.0f, 1.0f).pow(3.0f)).toLong()
}

private fun Long.toPercent(max: Long): Float {
    return (coerceIn(0, max).toFloat() / max).pow(1.0f / 3.0f)
}
