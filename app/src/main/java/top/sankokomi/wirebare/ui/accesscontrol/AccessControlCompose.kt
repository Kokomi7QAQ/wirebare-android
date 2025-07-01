package top.sankokomi.wirebare.ui.accesscontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.datastore.AccessControlDataStore
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.resources.AppCheckableMenu
import top.sankokomi.wirebare.ui.resources.AppRoundCornerBox
import top.sankokomi.wirebare.ui.resources.AppTitleBar
import top.sankokomi.wirebare.ui.resources.CheckableMenuItem
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.DynamicFloatImageButton
import top.sankokomi.wirebare.ui.resources.RealBox
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.RealRow
import top.sankokomi.wirebare.ui.resources.SwitchColors
import top.sankokomi.wirebare.ui.resources.Typographies
import top.sankokomi.wirebare.ui.resources.VisibleFadeInFadeOutAnimation
import top.sankokomi.wirebare.ui.util.AppData
import top.sankokomi.wirebare.ui.util.Global
import top.sankokomi.wirebare.ui.util.injectTouchEffect
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
fun AccessControlUI.AccessControlUIPage() {
    val allAppList = remember { mutableStateListOf<AppData>() }
    val accessControlList = remember { mutableStateListOf<AccessControlData>() }
    var accessCount by remember { mutableIntStateOf(0) }
    val showSystemAppItem = CheckableMenuItem(
        itemName = remember {
            mutableStateOf("显示系统应用")
        },
        checked = remember {
            mutableStateOf(ProxyPolicyDataStore.showSystemApp.value)
        },
        icon = R.drawable.ic_android_system
    )
    val selectAllAppItem = CheckableMenuItem(
        itemName = remember {
            mutableStateOf("全选")
        },
        checked = remember {
            mutableStateOf(false)
        },
        icon = R.drawable.ic_select_all
    )
    val listOperateMutex = remember { Mutex(false) }
    val rememberScope = rememberCoroutineScope()
    LaunchedEffect(selectAllAppItem.checked.value) {
        listOperateMutex.lock()
        withContext(Dispatchers.IO) {
            // 当全选选项被修改时
            val isSelectAllApp = selectAllAppItem.checked.value
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
    LaunchedEffect(showSystemAppItem.checked.value) {
        listOperateMutex.lock()
        withContext(Dispatchers.Default) {
            // 当是否显示系统应用选项被修改时
            val showSystemApp = showSystemAppItem.checked.value
            // 持久化当前是否显示系统应用
            ProxyPolicyDataStore.showSystemApp.value = showSystemApp
            accessCount = 0
            val allAppListTemp: List<AppData> = requireAppDataList().sorted()
            val appListTemp: List<AccessControlData> = allAppListTemp.filter {
                if (it.packageName == Global.appContext.packageName) {
                    false
                } else if (!showSystemApp) {
                    !it.isSystemApp
                } else {
                    true
                }
            }.map {
                val allow = AccessControlDataStore.collect(it.packageName)
                if (allow) {
                    accessCount++
                }
                AccessControlData(
                    it,
                    allow
                )
            }
            allAppList.mix(allAppListTemp)
            accessControlList.mix(appListTemp)
        }
        listOperateMutex.unlock()
    }
    LaunchedEffect(accessCount == accessControlList.size) {
        listOperateMutex.lock()
        selectAllAppItem.checked.value = accessCount == accessControlList.size
        listOperateMutex.unlock()
    }
    RealBox(
        Modifier.background(Colors.background)
    ) {
        RealColumn(
            modifier = Modifier.zIndex(1f)
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarHeightDp)
                    .background(Colors.background)
            )
            AppTitleBar(
                text = "访问控制",
                startContent = {
                    DynamicFloatImageButton(
                        painter = painterResource(id = R.drawable.ic_back)
                    ) {
                        finish()
                    }
                }
            )
        }
        RealColumn {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                item {
                    Spacer(modifier = Modifier.height(56.dp + statusBarHeightDp))
                    RealBox(
                        modifier = Modifier.height(6.dp)
                    ) {
                        VisibleFadeInFadeOutAnimation(visible = accessControlList.isEmpty()) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = Colors.onPrimary,
                                trackColor = Color.Transparent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AppRoundCornerBox {
                        AppCheckableMenu(
                            itemList = listOf(
                                showSystemAppItem,
                                selectAllAppItem
                            )
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
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
                            HorizontalDivider(
                                modifier = Modifier
                                    .background(Colors.onBackground)
                                    .padding(start = 56.dp, end = 16.dp)
                                    .fillMaxWidth()
                                    .height(0.2.dp)
                            )
                        }
                        RealRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .injectTouchEffect(normalBackground = Colors.onBackground)
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            AsyncImage(
                                model = accessControl.appData.appIcon,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .size(24.dp),
                                contentDescription = accessControl.appData.appName
                            )
                            RealColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                                    .align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = accessControl.appData.appName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .basicMarquee(),
                                    style = Typographies.titleSmall,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = accessControl.appData.packageName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .basicMarquee(),
                                    style = Typographies.bodySmall,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            }
                            Switch(
                                checked = accessControl.allow,
                                thumbContent = {
                                    Spacer(modifier = Modifier.size(999.dp))
                                },
                                colors = SwitchColors,
                                onCheckedChange = {
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
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}