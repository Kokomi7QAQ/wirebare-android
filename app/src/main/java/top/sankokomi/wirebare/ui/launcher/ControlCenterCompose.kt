package top.sankokomi.wirebare.ui.launcher

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.kernel.common.EventSynopsis
import top.sankokomi.wirebare.kernel.common.ProxyStatus
import top.sankokomi.wirebare.kernel.common.WireBareHelper
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.resources.AppCheckableItem
import top.sankokomi.wirebare.ui.resources.AppRoundCornerBox
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.util.statusBarHeightDp

@Composable
fun LauncherUI.PageControlCenter() {
    var wireBareStatus by remember { mutableStateOf(ProxyStatus.DEAD) }
    var maybeUnsupportedIPv6 by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        proxyStatusFlow.collect {
            maybeUnsupportedIPv6 = false
            wireBareStatus = it
        }
    }
    LaunchedEffect(Unit) {
        eventFlow.collect { event ->
            when (event.synopsis) {
                EventSynopsis.IPV6_UNREACHABLE -> {
                    maybeUnsupportedIPv6 = true
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
        ControlBox(wireBareStatus, maybeUnsupportedIPv6)
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun LauncherUI.ControlBox(
    status: ProxyStatus,
    maybeUnsupportedIPv6: Boolean
) {
    val switchChecked = remember { mutableStateOf(false) }
    val switchEnabled = remember { mutableStateOf(true) }
    val strSwitchDead = stringResource(R.string.control_center_main_switch_dead)
    val strSwitchLaunching = stringResource(R.string.control_center_main_switch_launching)
    val strSwitchActive = stringResource(R.string.control_center_main_switch_active)
    val strSwitchDying = stringResource(R.string.control_center_main_switch_dying)
    val switchSubName = remember { mutableStateOf(strSwitchDead) }
    val banFilter = ProxyPolicyDataStore.banAutoFilter.collectAsState()
    val autoFilterChecked = !banFilter.value
    val isWireBareActive = remember { mutableStateOf(false) }
    val enableSSL by ProxyPolicyDataStore.enableSSL.collectAsState()
    var systemTrustCert by remember { mutableStateOf(true) }
    LaunchedEffect(enableSSL) {
        systemTrustCert = if (enableSSL) {
            withContext(Dispatchers.IO) {
                WireBareHelper.checkSystemTrustCert(LauncherModel.wireBareJKS)
            }
        } else {
            true
        }
    }
    val strSSLDisable = stringResource(R.string.control_center_ssl_disable_desc)
    val strSSLEnable = stringResource(R.string.control_center_ssl_enable_desc)
    val strSSLWarning = stringResource(R.string.control_center_ssl_enable_warning)
    val enableSSLSubName = remember(enableSSL, systemTrustCert) {
        if (!enableSSL) {
            strSSLDisable
        } else {
            if (!systemTrustCert) strSSLWarning else strSSLEnable
        }
    }
    val strIPv6Disable = stringResource(R.string.control_center_ipv6_disable)
    val strIPv6Enable = stringResource(R.string.control_center_ipv6_enable)
    val strIPv6Warning = stringResource(R.string.control_center_ipv6_warning)
    val enableIPv6 = ProxyPolicyDataStore.enableIPv6.collectAsState()
    val enableIPv6SubName = remember(enableIPv6.value, maybeUnsupportedIPv6) {
        if (maybeUnsupportedIPv6) {
            strIPv6Warning
        } else {
            if (!enableIPv6.value) {
                strIPv6Disable
            } else {
                strIPv6Enable
            }
        }
    }
    val enableWakeLock by ProxyPolicyDataStore.enableWakeLock.collectAsState()
    LaunchedEffect(enableWakeLock) {
        if (enableWakeLock) {
            acquireWakeLock()
        } else {
            releaseWakeLock()
        }
    }
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
                    itemName = stringResource(R.string.control_center_auto_filter),
                    checked = autoFilterChecked,
                    icon = R.drawable.ic_filter,
                    subName = stringResource(R.string.control_center_auto_filter_desc),
                    enabled = !isWireBareActive.value
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
                    itemName = stringResource(R.string.control_center_ssl),
                    checked = enableSSL,
                    icon = R.drawable.ic_cert,
                    subName = enableSSLSubName,
                    isWarning = enableSSL && !systemTrustCert,
                    enabled = !isWireBareActive.value
                ) {
                    ProxyPolicyDataStore.enableSSL.value = !ProxyPolicyDataStore.enableSSL.value
                }
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 48.dp, end = 16.dp)
                        .fillMaxWidth()
                        .height(0.2.dp),
                    color = Colors.background
                )
                AppCheckableItem(
                    itemName = stringResource(R.string.control_center_ipv6_proxy),
                    checked = enableIPv6.value,
                    icon = R.drawable.ic_ipv6,
                    subName = enableIPv6SubName,
                    isWarning = maybeUnsupportedIPv6,
                    enabled = !isWireBareActive.value
                ) {
                    ProxyPolicyDataStore.enableIPv6.value = !ProxyPolicyDataStore.enableIPv6.value
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppRoundCornerBox {
            RealColumn {
                AppCheckableItem(
                    itemName = stringResource(R.string.control_center_wake_lock),
                    checked = enableWakeLock,
                    icon = R.drawable.ic_battery,
                    subName = stringResource(R.string.control_center_wake_lock_desc),
                    enabled = true
                ) {
                    ProxyPolicyDataStore.enableWakeLock.value =
                        !ProxyPolicyDataStore.enableWakeLock.value
                }
            }
        }
    }
}