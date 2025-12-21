package top.sankokomi.wirebare.ui.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.kernel.common.ProxyStatus
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.datastore.AccessControlDataStore
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.resources.AppCheckableItem
import top.sankokomi.wirebare.ui.resources.AppRoundCornerBox
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.RealBox
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.Typographies
import top.sankokomi.wirebare.ui.resources.VisibleFadeInFadeOutAnimation
import top.sankokomi.wirebare.ui.util.AppData
import top.sankokomi.wirebare.ui.util.Global
import top.sankokomi.wirebare.ui.util.mix
import top.sankokomi.wirebare.ui.util.requireAppDataList
import top.sankokomi.wirebare.ui.util.statusBarHeightDp

@Stable
data class AccessControlData(
    val appData: AppData,
    val allow: Boolean
) : Comparable<AccessControlData> {
    override fun compareTo(other: AccessControlData): Int {
        return appData.compareTo(other.appData)
    }
}

@Composable
fun LauncherUI.AccessControlPage() {
    val rememberScope = rememberCoroutineScope()
    val listOperateMutex = remember { Mutex(false) }
    val allAppList = remember { mutableStateListOf<AppData>() }
    val accessControlList = remember { mutableStateListOf<AccessControlData>() }
    var accessCount by remember { mutableIntStateOf(0) }
    val showSystemAppItemChecked = ProxyPolicyDataStore.showSystemApp.collectAsState()
    val selectAllAppItemChecked = remember { mutableStateOf(false) }
    var wireBareStatus by remember { mutableStateOf(ProxyStatus.DEAD) }
    LaunchedEffect(showSystemAppItemChecked.value) {
        withContext(Dispatchers.IO) {
            listOperateMutex.lock()
            val showSystemApp = showSystemAppItemChecked.value
            accessCount = 0
            val allAppListTemp = requireAppDataList().sorted()
            val appListTemp = allAppListTemp.filter {
                when {
                    it.packageName == Global.appContext.packageName -> false
                    !showSystemApp -> !it.isSystemApp
                    else -> true
                }
            }.map {
                val allow = AccessControlDataStore.collect(it.packageName)
                if (allow) accessCount++
                AccessControlData(it, allow)
            }
            allAppList.mix(allAppListTemp)
            accessControlList.mix(appListTemp)
            listOperateMutex.unlock()
        }
    }
    LaunchedEffect(accessCount == accessControlList.size) {
        listOperateMutex.lock()
        selectAllAppItemChecked.value = accessCount == accessControlList.size
        listOperateMutex.unlock()
    }
    LaunchedEffect(Unit) {
        proxyStatusFlow.collect {
            wireBareStatus = it
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.background),
    ) {
        item {
            Spacer(modifier = Modifier.height(56.dp + statusBarHeightDp))
            AppRoundCornerBox {
                RealColumn {
                    AppCheckableItem(
                        icon = R.drawable.ic_android_system,
                        itemName = stringResource(R.string.access_control_show_sys_app),
                        checked = showSystemAppItemChecked.value
                    ) { showSystemApp ->
                        rememberScope.launch {
                            ProxyPolicyDataStore.showSystemApp.value = showSystemApp
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(start = 56.dp, end = 16.dp)
                            .fillMaxWidth()
                            .height(0.2.dp),
                        color = Colors.background
                    )
                    AppCheckableItem(
                        icon = R.drawable.ic_select_all,
                        itemName = stringResource(R.string.access_control_select_all),
                        checked = selectAllAppItemChecked.value,
                        enabled = wireBareStatus == ProxyStatus.DEAD
                    ) { isSelectAllApp ->
                        rememberScope.launch {
                            listOperateMutex.lock()
                            withContext(Dispatchers.IO) {
                                if (isSelectAllApp && accessCount < accessControlList.size) {
                                    // 若新选项是全选且当前没有全选
                                    AccessControlDataStore.emitAll(
                                        accessControlList.map {
                                            it.appData.packageName to true
                                        }
                                    )
                                    accessControlList.replaceAll { it.copy(allow = true) }
                                    accessCount = accessControlList.size
                                } else if (!isSelectAllApp && accessCount >= accessControlList.size) {
                                    // 若新选项是全不选且当前不是全不选
                                    withContext(Dispatchers.IO) {
                                        AccessControlDataStore.emitAll(
                                            allAppList.map {
                                                it.packageName to false
                                            }
                                        )
                                        accessControlList.replaceAll { it.copy(allow = false) }
                                    }
                                    accessCount = 0
                                }
                            }
                            listOperateMutex.unlock()
                        }
                    }
                }
            }
        }
        item {
            RealColumn {
                Spacer(modifier = Modifier.height(4.dp))
                RealBox(
                    modifier = Modifier.height(4.dp)
                ) {
                    VisibleFadeInFadeOutAnimation(visible = accessControlList.isEmpty()) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Colors.primaryContainer,
                            trackColor = Color.Transparent
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (wireBareStatus != ProxyStatus.DEAD) {
                    Text(
                        text = stringResource(R.string.access_control_modify_desc),
                        modifier = Modifier.padding(horizontal = 32.dp),
                        color = Colors.error,
                        style = Typographies.bodyMedium
                    )
                } else {
                    Text(
                        text = stringResource(R.string.access_control_list_desc),
                        modifier = Modifier.padding(horizontal = 32.dp),
                        style = Typographies.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        items(accessControlList.size) { index ->
            val accessControl = accessControlList[index]
            val itemShape = if (accessControlList.size == 1) {
                RoundedCornerShape(size = 24.dp)
            } else {
                when (index) {
                    0 -> {
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    }

                    accessControlList.size - 1 -> {
                        RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    }

                    else -> RectangleShape
                }
            }
            RealColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(itemShape)
                    .animateItem()
            ) {
                if (index != 0) {
                    RealBox {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.2.dp),
                            color = Colors.onBackground
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(start = 56.dp, end = 16.dp)
                                .fillMaxWidth()
                                .height(0.2.dp),
                            color = Colors.background
                        )
                    }
                }
                AppCheckableItem(
                    icon = accessControl.appData.appIcon,
                    itemName = accessControl.appData.appName,
                    tint = null,
                    checked = accessControl.allow,
                    enabled = wireBareStatus == ProxyStatus.DEAD,
                    subName = accessControl.appData.packageName
                ) {
                    rememberScope.launch(Dispatchers.IO) {
                        listOperateMutex.lock()
                        val allow = accessControl.allow
                        withContext(Dispatchers.Main) {
                            accessControlList[index] =
                                accessControl.copy(allow = !allow)
                            if (!allow) accessCount++ else accessCount--
                        }
                        AccessControlDataStore.emit(accessControl.appData.packageName to !allow)
                        listOperateMutex.unlock()
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}