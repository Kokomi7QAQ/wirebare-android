package top.sankokomi.wirebare.ui.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.resources.RedZ

@Stable
data class AppData(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean
) : Comparable<AppData> {

    companion object {
        val invalid = AppData(none(), none(), false)

        val all: List<AppData> by lazy {
            Global.appContext.packageManager.getInstalledApplications(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PackageManager.MATCH_UNINSTALLED_PACKAGES
                } else {
                    @Suppress("DEPRECATION")
                    PackageManager.GET_UNINSTALLED_PACKAGES
                }
            ).map(::from)
        }

        fun from(uid: Int): AppData {
            if (uid == Process.INVALID_UID) {
                return invalid
            }
            val pkg = Global.appContext.packageManager.getPackagesForUid(
                uid
            )?.firstOrNull() ?: return invalid
            val appInfo = try {
                Global.appContext.packageManager.getApplicationInfo(
                    pkg,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        PackageManager.MATCH_UNINSTALLED_PACKAGES
                    } else {
                        @Suppress("DEPRECATION")
                        PackageManager.GET_UNINSTALLED_PACKAGES
                    }
                )
            } catch (_: PackageManager.NameNotFoundException) {
                return invalid
            }
            return from(appInfo)
        }

        fun from(appInfo: ApplicationInfo?): AppData {
            appInfo ?: return invalid
            return AppData(
                Global.appContext.packageManager.getApplicationLabel(appInfo).toString(),
                appInfo.packageName,
                appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
            )
        }
    }

    override fun compareTo(other: AppData): Int {
        return packageName.compareTo(other.packageName)
    }
}

class AppIconFetcher(
    private val data: AppData,
    private val options: Options
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val icon = withContext(Dispatchers.IO) {
            if (data === AppData.invalid) {
                defaultDrawable()
            } else {
                try {
                    options.context.packageManager.getApplicationIcon(data.packageName)
                } catch (_: Exception) {
                    defaultDrawable()
                }
            }
        }
        return DrawableResult(
            drawable = icon,
            isSampled = false,
            dataSource = DataSource.NETWORK
        )
    }

    private fun defaultDrawable(): Drawable {
        return ResourcesCompat.getDrawable(
            options.context.resources,
            R.drawable.ic_unknow, null
        )?.also {
            it.setTint(RedZ.toArgb())
        } ?: Color.TRANSPARENT.toDrawable()
    }

    class Factory : Fetcher.Factory<AppData> {
        override fun create(data: AppData, options: Options, imageLoader: ImageLoader): Fetcher {
            return AppIconFetcher(data, options)
        }
    }
}
