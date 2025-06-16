package top.sankokomi.wirebare.ui.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import androidx.compose.runtime.Stable

@Stable
data class AppData(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean
) : Comparable<AppData> {
    val appIcon by lazy {
        Global.appContext.packageManager.getApplicationIcon(packageName)
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