package top.sankokomi.wirebare.ui.window

import android.content.Intent
import android.util.Size
import android.view.View
import androidx.compose.runtime.Composable
import androidx.lifecycle.LifecycleService
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import top.sankokomi.wirebare.ui.util.screenHeight
import top.sankokomi.wirebare.ui.util.screenWidth

abstract class ComposeWindowService : LifecycleService(), SavedStateRegistryOwner {

    companion object {
        const val ACTION_SHOW_WINDOW = "top.sankokomi.wirebare.ui.action.ShowWindow"
        const val ACTION_HIDE_WINDOW = "top.sankokomi.wirebare.ui.action.HideWindow"
    }

    protected abstract val tag: String
    protected abstract val size: Size
    protected open val originX: Int = 0
    protected open val originY: Int = 0
    protected open val onClickListener: View.OnClickListener = View.OnClickListener { }
    protected open val onLocationChangedListener: (Int, Int) -> Unit = { _, _ -> }
    protected abstract val content: @Composable () -> Unit

    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private var composeWindow: DragComposeWindow? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_SHOW_WINDOW -> showWindow()
            ACTION_HIDE_WINDOW -> hideWindow()
        }
        return START_STICKY
    }

    private fun showWindow() {
        if (composeWindow != null) return
        composeWindow = DragComposeWindow(this).also { dragView ->
            dragView.setup(
                size = size,
                x = originX,
                y = originY,
                lifecycleOwner = this,
                savedStateRegistryOwner = this,
                content = content
            )
            dragView.setOnClickListener {
                onClickListener.onClick(it)
            }
            dragView.setOnLocationChangedListener { x, y ->
                onLocationChangedListener(x, y)
            }
        }
    }

    protected fun updateWindow() {
        composeWindow?.resetSize(size = size)
    }

    private fun hideWindow() {
        composeWindow?.remove()
        composeWindow = null
        stopSelf()
    }

    override fun onDestroy() {
        composeWindow?.remove()
        composeWindow = null
        super.onDestroy()
    }
}