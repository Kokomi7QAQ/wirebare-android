package top.sankokomi.wirebare.ui.launcher

import android.os.Bundle
import android.os.PowerManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.kernel.common.IImportantEventListener
import top.sankokomi.wirebare.kernel.common.IProxyStatusListener
import top.sankokomi.wirebare.kernel.common.ImportantEvent
import top.sankokomi.wirebare.kernel.common.ProxyStatus
import top.sankokomi.wirebare.kernel.common.VpnPrepareActivity
import top.sankokomi.wirebare.kernel.common.WireBare
import top.sankokomi.wirebare.ui.datastore.AccessControlDataStore
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.record.HttpRecorder
import top.sankokomi.wirebare.ui.record.HttpReq
import top.sankokomi.wirebare.ui.record.HttpRsp
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.WirebareUITheme
import top.sankokomi.wirebare.ui.util.requireAppDataList

class LauncherUI : VpnPrepareActivity() {

    private val _proxyStatusFlow = MutableStateFlow(ProxyStatus.DEAD)

    private val _eventFlow = MutableSharedFlow<ImportantEvent>(
        0, 1, BufferOverflow.SUSPEND
    )

    private val _requestFlow = MutableSharedFlow<HttpReq>()

    private val _responseFlow = MutableSharedFlow<HttpRsp>()

    val proxyStatusFlow = _proxyStatusFlow.asStateFlow()

    val eventFlow = _eventFlow.asSharedFlow()

    val requestFlow = _requestFlow.asSharedFlow()

    val responseFlow = _responseFlow.asSharedFlow()

    private var isWakeAcquired = false

    private val wakeLock by lazy {
        getSystemService<PowerManager>()?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WireBare::LauncherUI"
        )
    }

    fun startProxy() {
        prepareProxy()
    }

    fun stopProxy() {
        WireBare.stopProxy()
    }

    @Suppress("WakelockTimeout")
    fun acquireWakeLock() {
        if (!isWakeAcquired) {
            isWakeAcquired = true
            wakeLock?.acquire()
        }
    }

    fun releaseWakeLock() {
        if (isWakeAcquired) {
            wakeLock?.release()
            isWakeAcquired = false
        }
    }

    override fun onPrepareSuccess() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 提前设定状态为正在启动
            _proxyStatusFlow.value = ProxyStatus.STARTING
            val showSystemApp = ProxyPolicyDataStore.showSystemApp.value
            val appList = requireAppDataList().filter {
                if (!showSystemApp) {
                    !it.isSystemApp
                } else {
                    true
                }
            }
            val accessList = AccessControlDataStore.collectAll(
                appList.map { app -> app.packageName }
            ).mapIndexedNotNull { index, b -> if (b) appList[index].packageName else null }
            withContext(Dispatchers.Main) {
                LauncherModel.startProxy(
                    accessList.toTypedArray(),
                    onRequest = {
                        lifecycleScope.launch {
                            _requestFlow.emit(HttpReq.from(it))
                        }
                    },
                    onResponse = {
                        lifecycleScope.launch {
                            _responseFlow.emit(HttpRsp.from(it))
                        }
                    }
                )
            }
        }
    }

    private val wireBareStatusListener = object : IProxyStatusListener {
        override fun onVpnStatusChanged(oldStatus: ProxyStatus, newStatus: ProxyStatus): Boolean {
            _proxyStatusFlow.value = newStatus
            return false
        }
    }

    private val wireBareEventListener = object : IImportantEventListener {
        override fun onPost(event: ImportantEvent) {
            lifecycleScope.launch {
                _eventFlow.emit(event)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 添加 WireBare 状态监听器
        WireBare.addVpnProxyStatusListener(wireBareStatusListener)
        WireBare.addImportantEventListener(wireBareEventListener)
        enableEdgeToEdge()
        setContent {
            WirebareUITheme(
                statusBarColor = { it.background },
                navigationBarColor = { it.background }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Colors.background
                ) {
                    WireBareUIPage()
                }
            }
        }
    }

    fun queryRecord() {
        lifecycleScope.launch {
            HttpRecorder.queryRequestRecord().forEach {
                _requestFlow.emit(it)
            }
            HttpRecorder.queryResponseRecord().forEach {
                _responseFlow.emit(it)
            }
        }
    }

    override fun onDestroy() {
        // 解除监听，防止内存泄露
        releaseWakeLock()
        WireBare.removeImportantEventListener(wireBareEventListener)
        WireBare.removeVpnProxyStatusListener(wireBareStatusListener)
        super.onDestroy()
    }
}
