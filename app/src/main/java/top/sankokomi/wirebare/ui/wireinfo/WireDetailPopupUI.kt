package top.sankokomi.wirebare.ui.wireinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.resources.AppExpandableTextItem
import top.sankokomi.wirebare.ui.resources.AppRoundCornerBox
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.Transparent
import top.sankokomi.wirebare.ui.resources.VisibleFadeInFadeOutAnimation
import top.sankokomi.wirebare.ui.resources.WirebareUITheme
import top.sankokomi.wirebare.ui.util.copyTextToClipBoard
import top.sankokomi.wirebare.ui.util.navigationBarHeightDp
import top.sankokomi.wirebare.ui.util.showToast
import top.sankokomi.wirebare.ui.util.statusBarHeightDp

class WireDetailPopupUI : ComponentActivity() {

    private var visible: MutableState<Boolean>? = null

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""
        enableEdgeToEdge()
        setContent {
            WirebareUITheme(
                isShowStatusBar = false,
                isShowNavigationBar = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Transparent
                ) {
                    val visible = remember { mutableStateOf(false) }
                    this.visible = visible
                    val rememberScope = rememberCoroutineScope()
                    LaunchedEffect(Unit) {
                        visible.value = true
                    }
                    VisibleFadeInFadeOutAnimation(visible.value) {
                        RealColumn(
                            modifier = Modifier
                                .background(Colors.background.copy(alpha = 0.6f))
                                .verticalScroll(rememberScrollState())
                                .clickable {
                                    if (visible.value) {
                                        visible.value = false
                                        rememberScope.launch {
                                            delay(300L)
                                            finish()
                                        }
                                    }
                                },
                            verticalArrangement = Arrangement.Center
                        ) {
                            Spacer(modifier = Modifier.height(56.dp + statusBarHeightDp))
                            RealColumn(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .combinedClickable(
                                        onLongClick = {
                                            copyTextToClipBoard(content)
                                            showToast(R.string.req_rsp_info_copy_success)
                                        },
                                        onClick = {
                                        }
                                    ),
                                verticalArrangement = Arrangement.Center
                            ) {
                                AppRoundCornerBox(
                                    background = Colors.primary
                                ) {
                                    AppExpandableTextItem(
                                        icon = R.drawable.ic_wirebare,
                                        title = title,
                                        body = content,
                                        expand = false,
                                        maxLinesInClosed = Int.MAX_VALUE
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(56.dp + navigationBarHeightDp))
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val visible = visible ?: return super.onBackPressed()
        if (visible.value) {
            visible.value = false
            lifecycleScope.launch {
                delay(300L)
                finish()
            }
        }
    }
}
