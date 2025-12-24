package top.sankokomi.wirebare.ui.knet

import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import top.sankokomi.wirebare.kernel.dashboard.WireBareDashboard
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.resources.Transparent
import top.sankokomi.wirebare.ui.resources.WirebareUIFloatingWindowTheme
import top.sankokomi.wirebare.ui.util.dpToPx

class KnetFloatingDashboardService : LifecycleService(), SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: FrameLayout
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    companion object {
        const val ACTION_SHOW = "top.sankokomi.wirebare.action.SHOW_FLOATING_WINDOW"
        const val ACTION_HIDE = "top.sankokomi.wirebare.action.HIDE_FLOATING_WINDOW"
    }

    private val savedStateRegistryController =
        SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_SHOW -> showFloatingWindow()
            ACTION_HIDE -> hideFloatingWindow()
        }
        return START_STICKY
    }

    private fun showFloatingWindow() {
        if (::floatingView.isInitialized) {
            return
        }

        // 创建悬浮窗布局
        floatingView = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.TRANSPARENT)
            setViewTreeLifecycleOwner(this@KnetFloatingDashboardService)
            setViewTreeSavedStateRegistryOwner(this@KnetFloatingDashboardService)
        }

        // 创建ComposeView并设置内容
        val composeView = ComposeView(this).apply {
            setContent {
                WirebareUIFloatingWindowTheme(
                    isShowStatusBar = false,
                    isShowNavigationBar = false,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Transparent
                    ) {
                        MiniBandwidthChart(
                            itemName = stringResource(R.string.knet_bandwidth),
                            subName = "",
                            WireBareDashboard.bandwidthFlow
                        )
                    }
                }
            }
        }

        val touchView = View(this)

        floatingView.addView(composeView)
        floatingView.addView(touchView)
        touchView.bringToFront()

        // 设置窗口参数
        val params = WindowManager.LayoutParams().apply {
            width = getScreenWidth() / 2 + 16.dp.dpToPx
            height = width * 2 / 3 + 16.dp.dpToPx

            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        // 添加触摸事件监听器，实现拖动功能
        touchView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }

                else -> false
            }
        }

        // 添加悬浮窗到窗口管理器
        windowManager.addView(floatingView, params)
    }

    private fun hideFloatingWindow() {
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        stopSelf()
    }

    private fun getScreenWidth(): Int {
        return resources.displayMetrics.widthPixels
    }

    private fun getScreenHeight(): Int {
        return resources.displayMetrics.heightPixels
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onDestroy() {
        hideFloatingWindow()
        super.onDestroy()
    }

}