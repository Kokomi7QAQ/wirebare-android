package top.sankokomi.wirebare.ui.accesscontrol

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import top.sankokomi.wirebare.ui.resources.LightGreen
import top.sankokomi.wirebare.ui.resources.LightGrey
import top.sankokomi.wirebare.ui.resources.MediumGreen
import top.sankokomi.wirebare.ui.resources.MediumGrey
import top.sankokomi.wirebare.ui.resources.Purple80
import top.sankokomi.wirebare.ui.resources.RealBox
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.RealRow
import top.sankokomi.wirebare.ui.util.AppData
import top.sankokomi.wirebare.ui.util.Global
import top.sankokomi.wirebare.ui.util.requireAppDataList

@Composable
fun AccessControlUI.AccessControlUIPage() {
    val allAppList = remember { mutableStateListOf<AppData>() }
    val appList = remember { mutableStateListOf<AppData>() }
    val accessControlList = remember { mutableStateListOf<Boolean>() }
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
        // 当全选选项被修改时
        val isSelectAllApp = selectAllAppItem.checked.value
        if (isSelectAllApp && accessCount < accessControlList.size) {
            // 若新选项是全选且当前没有全选
            withContext(Dispatchers.IO) {
                AccessControlDataStore.emitAll(
                    appList.map {
                        it.packageName to true
                    }
                )
            }
            accessControlList.replaceAll { true }
            accessCount = accessControlList.size
        } else if (!isSelectAllApp && accessCount >= accessControlList.size) {
            // 若新选项是全不选且当前不是全不选
            withContext(Dispatchers.IO) {
                AccessControlDataStore.emitAll(
                    allAppList.map {
                        it.packageName to false
                    }
                )
            }
            accessControlList.replaceAll { false }
            accessCount = 0
        }
        listOperateMutex.unlock()
    }
    LaunchedEffect(showSystemAppItem.checked.value) {
        listOperateMutex.lock()
        // 当是否显示系统应用选项被修改时
        val showSystemApp = showSystemAppItem.checked.value
        // 持久化当前是否显示系统应用
        ProxyPolicyDataStore.showSystemApp.value = showSystemApp
        accessCount = 0
        allAppList.clear()
        appList.clear()
        accessControlList.clear()
        val allAppListTemp: List<AppData>
        val appListTemp: List<AppData>
        withContext(Dispatchers.Default) {
            allAppListTemp = requireAppDataList().sorted()
            appListTemp = allAppListTemp.filter {
                if (it.packageName == Global.appContext.packageName) {
                    false
                } else if (!showSystemApp) {
                    !it.isSystemApp
                } else {
                    true
                }
            }
        }
        val acList = withContext(Dispatchers.IO) {
            AccessControlDataStore.collectAll(
                appListTemp.map { it.packageName }
            )
        }
        var count = 0
        acList.onEach {
            if (it) count++
        }
        accessCount = count
        allAppList.addAll(allAppListTemp)
        appList.addAll(appListTemp)
        accessControlList.addAll(acList)
        listOperateMutex.unlock()
    }
    LaunchedEffect(accessCount) {
        listOperateMutex.lock()
        selectAllAppItem.checked.value = accessCount == accessControlList.size
        listOperateMutex.unlock()
    }
    RealColumn(
        Modifier.background(LightGrey)
    ) {
        AppTitleBar(
            text = "访问控制"
        )
        RealBox(
            modifier = Modifier.height(6.dp)
        ) {
            if (accessControlList.isEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Purple80,
                    trackColor = Color.Transparent
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
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
                val appData = appList[index]
                val accessControl = accessControlList[index]
                val itemShape = when (index) {
                    0 -> {
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    }

                    accessControlList.size - 1 -> {
                        RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    }

                    else -> RectangleShape
                }
                RealColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(itemShape)
                        .background(if (accessControl) LightGreen else Color.White)
                ) {
                    if (index != 0) {
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(start = 56.dp, end = 16.dp)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(LightGrey)
                        )
                    }
                    RealRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                rememberScope.launch(Dispatchers.IO) {
                                    listOperateMutex.lock()
                                    AccessControlDataStore.emit(appData.packageName to !accessControl)
                                    withContext(Dispatchers.Main) {
                                        accessControlList[index] = !accessControl
                                        if (!accessControl) accessCount++ else accessCount--
                                    }
                                    listOperateMutex.unlock()
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        AsyncImage(
                            model = appData.appIcon,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(24.dp),
                            contentDescription = appData.appName
                        )
                        RealColumn(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Text(
                                text = appData.appName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee(),
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 14.sp,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = appData.packageName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .basicMarquee(),
                                color = MediumGrey,
                                fontSize = 10.sp,
                                lineHeight = 10.sp,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                        Icon(
                            painter = painterResource(R.drawable.ic_wirebare),
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(24.dp),
                            tint = if (accessControl) MediumGreen else Color.Transparent,
                            contentDescription = null
                        )
                    }
                }

//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 4.dp)
//                        .clip(RoundedCornerShape(6.dp))
//                        .clickable {
//                            rememberScope.launch(Dispatchers.IO) {
//                                listOperateMutex.lock()
//                                AccessControlDataStore.emit(appData.packageName to !accessControl)
//                                withContext(Dispatchers.Main) {
//                                    accessControlList[index] = !accessControl
//                                    if (!accessControl) accessCount++ else accessCount--
//                                }
//                                listOperateMutex.unlock()
//                            }
//                        }
//                ) {
//                    SmallColorfulText(
//                        mainText = appData.appName,
//                        subText = appData.packageName,
//                        backgroundColor = Purple80,
//                        textColor = Color.Black
//                    )
//                    Checkbox(
//                        checked = accessControl,
//                        onCheckedChange = null,
//                        modifier = Modifier
//                            .align(Alignment.CenterEnd)
//                            .padding(end = 16.dp)
//                    )
//                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}