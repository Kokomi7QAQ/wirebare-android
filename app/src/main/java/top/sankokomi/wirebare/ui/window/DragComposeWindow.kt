package top.sankokomi.wirebare.ui.window

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Size
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import top.sankokomi.wirebare.ui.util.screenHeight
import top.sankokomi.wirebare.ui.util.screenWidth

class DragComposeWindow(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(
    context, attrs, defStyle
) {

    private val windowManager = context.getSystemService<WindowManager>()!!

    private val windowParams = WindowManager.LayoutParams().also {
        it.type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        it.format = PixelFormat.TRANSLUCENT
        it.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

        it.gravity = Gravity.CENTER
        it.x = 0
        it.y = 0
    }

    private val composeView = ComposeView(context).also {
        this@DragComposeWindow.addView(it)
    }

    private var hasViewAdded = false
    private var rangeX = 0..0
    private var rangeY = 0..0
    private var downTime = 0L
    private var downX = 0.0f
    private var downY = 0.0f
    private var lastX = 0.0f
    private var lastY = 0.0f
    private var onChanged: (x: Int, y: Int) -> Unit = { _, _ -> }

    fun setup(
        size: Size,
        rangeX: IntRange = -(screenWidth - size.width) / 2..(screenWidth - size.width) / 2,
        rangeY: IntRange = -(screenHeight - size.height) / 2..(screenHeight - size.height) / 2,
        x: Int = windowParams.x,
        y: Int = windowParams.y,
        lifecycleOwner: LifecycleOwner,
        savedStateRegistryOwner: SavedStateRegistryOwner,
        content: @Composable () -> Unit
    ) {
        windowParams.width = size.width
        windowParams.height = size.height
        this.rangeX = rangeX
        this.rangeY = rangeY
        windowParams.x = x.coerceIn(rangeX)
        windowParams.y = y.coerceIn(rangeY)
        setViewTreeLifecycleOwner(lifecycleOwner)
        setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
        composeView.setContent(content)
        if (hasViewAdded) {
            windowManager.removeView(this)
        }
        windowManager.addView(this, windowParams)
        hasViewAdded = true
    }

    fun resetSize(
        size: Size,
        rangeX: IntRange = -(screenWidth - size.width) / 2..(screenWidth - size.width) / 2,
        rangeY: IntRange = -(screenHeight - size.height) / 2..(screenHeight - size.height) / 2,
        x: Int = windowParams.x.coerceIn(rangeX),
        y: Int = windowParams.y.coerceIn(rangeY)
    ) {
        windowParams.width = size.width
        windowParams.height = size.height
        this.rangeX = rangeX
        this.rangeY = rangeY
        windowParams.x = x.coerceIn(rangeX)
        windowParams.y = y.coerceIn(rangeY)
        if (hasViewAdded) {
            windowManager.updateViewLayout(this, windowParams)
            onChanged(windowParams.x, windowParams.y)
        }
    }

    fun setOnLocationChangedListener(onChanged: (x: Int, y: Int) -> Unit) {
        this.onChanged = onChanged
    }

    fun remove() {
        if (hasViewAdded) {
            windowManager.removeView(this)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        val result = when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downTime = SystemClock.uptimeMillis()
                downX = event.rawX
                downY = event.rawY
                true
            }

            MotionEvent.ACTION_MOVE -> {
                windowParams.x += ((event.rawX - lastX).toInt())
                windowParams.y += ((event.rawY - lastY).toInt())
                windowParams.x = windowParams.x.coerceIn(rangeX)
                windowParams.y = windowParams.y.coerceIn(rangeY)
                if (hasViewAdded) {
                    windowManager.updateViewLayout(this, windowParams)
                    onChanged(windowParams.x, windowParams.y)
                }
                true
            }

            MotionEvent.ACTION_UP -> {
                if (
                    downX == event.rawX &&
                    downY == event.rawY &&
                    downTime >= SystemClock.uptimeMillis() - 300
                ) {
                    performClick()
                }
                downTime = 0L
                downX = 0.0f
                downY = 0.0f
                true
            }

            else -> false
        }
        lastX = event.rawX
        lastY = event.rawY
        return result
    }
}