package top.sankokomi.wirebare.ui.util

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

fun ComponentActivity.immersive() {
    val transparentStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
    enableEdgeToEdge(
        statusBarStyle = transparentStyle,
        navigationBarStyle = transparentStyle
    )
    // transparent navigation bar
    window.isNavigationBarContrastEnforced = false
}

val screenWidth: Int
    get() = Global.appContext.resources.displayMetrics.widthPixels

val screenHeight: Int
    get() = Global.appContext.resources.displayMetrics.heightPixels
