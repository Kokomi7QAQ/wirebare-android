package top.sankokomi.wirebare.ui.window

import android.content.Intent
import android.util.Size
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import top.sankokomi.wirebare.kernel.dashboard.WireBareDashboard
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.datastore.KnetPolicyDataStore
import top.sankokomi.wirebare.ui.knet.MiniBandwidthChart
import top.sankokomi.wirebare.ui.resources.AppRoundCornerBox
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.RealBox
import top.sankokomi.wirebare.ui.resources.WirebareUITheme
import top.sankokomi.wirebare.ui.util.Global
import top.sankokomi.wirebare.ui.util.dpToPx
import top.sankokomi.wirebare.ui.util.screenWidth
import top.sankokomi.wirebare.ui.util.spToPx

class KnetDashboardWindowService : ComposeWindowService() {

    companion object {
        fun auto() {
            Global.scope.launch {
                KnetPolicyDataStore.enableFloatingWindow.collect {
                    showOrHide()
                }
            }
            Global.scope.launch {
                KnetPolicyDataStore.enableFloatingWindowOnlyInBackground.collect {
                    showOrHide()
                }
            }
            Global.scope.launch {
                Global.appState.collect {
                    showOrHide()
                }
            }
        }

        private fun showOrHide() {
            var show = false
            if (KnetPolicyDataStore.enableFloatingWindow.value) {
                if (KnetPolicyDataStore.enableFloatingWindowOnlyInBackground.value) {
                    if (Global.appState.value == Global.AppState.BACKGROUND) {
                        show = true
                    }
                } else {
                    show = true
                }
            }
            val intent = Intent(Global.appContext, KnetDashboardWindowService::class.java)
            if (show) {
                intent.action = ACTION_SHOW_WINDOW
            } else {
                intent.action = ACTION_HIDE_WINDOW
            }
            Global.appContext.startService(intent)
        }
    }

    init {
        Global.scope.launch {
            KnetPolicyDataStore.enableMiniFloatingWindow.collect {
                updateWindow()
            }
        }
    }

    override val tag: String = "KnetDashboardWindowService"

    override val size: Size
        get() {
            if (KnetPolicyDataStore.enableMiniFloatingWindow.value) {
                val width = screenWidth / 3
                val height = 20.spToPx + 16.dpToPx
                return Size(width + 8.dpToPx, height + 8.dpToPx)
            } else {
                val width = screenWidth / 2
                val height = width / 3 * 2
                return Size(width + 8.dpToPx, height + 8.dpToPx)
            }
        }

    override val originX: Int = KnetPolicyDataStore.floatingWindowX.value
    override val originY: Int = KnetPolicyDataStore.floatingWindowY.value

    override val onClickListener: View.OnClickListener = {
        KnetPolicyDataStore.enableMiniFloatingWindow.value =
            !KnetPolicyDataStore.enableMiniFloatingWindow.value
    }

    override val onLocationChangedListener: (Int, Int) -> Unit = { x, y ->
        KnetPolicyDataStore.floatingWindowX.value = x
        KnetPolicyDataStore.floatingWindowY.value = y
    }

    override val content: @Composable (() -> Unit) = {
        WirebareUITheme(
            isShowStatusBar = false,
            isShowNavigationBar = false,
            transparentBackground = true,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Colors.background
            ) {
                RealBox(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Colors.primaryContainer)
                        .padding(4.dp)
                ) {
                    AppRoundCornerBox(
                        paddingHorizontal = 0.dp,
                        corner = 12.dp,
                        background = Colors.onBackground
                    ) {
                        MiniBandwidthChart(
                            itemName = stringResource(R.string.knet_bandwidth),
                            WireBareDashboard.bandwidthFlow
                        )
                    }
                }
            }
        }
    }
}