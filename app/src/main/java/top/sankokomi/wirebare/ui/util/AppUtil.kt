package top.sankokomi.wirebare.ui.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import androidx.compose.runtime.Stable
import androidx.core.graphics.drawable.toDrawable

@Stable
data class AppData(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean
) : Comparable<AppData> {
    val appIcon by lazy {
        try {
            Global.appContext.packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            return@lazy Color.TRANSPARENT.toDrawable()
        }
    }

    override fun compareTo(other: AppData): Int {
        return packageName.compareTo(other.packageName)
    }
}

fun requireAppDataList(): List<AppData> {
    return Global.appContext.packageManager.getInstalledApplications(
        PackageManager.MATCH_UNINSTALLED_PACKAGES
    ).map {
        AppData(
            Global.appContext.packageManager.getApplicationLabel(it).toString(),
            it.packageName,
            it.flags and ApplicationInfo.FLAG_SYSTEM != 0
        )
    }
}