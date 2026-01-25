package top.sankokomi.wirebare.ui.app

import android.app.Application
import coil.Coil
import coil.ImageLoader
import top.sankokomi.wirebare.ui.util.AppIconFetcher
import top.sankokomi.wirebare.ui.util.Global

class WireBareUIApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Global.attach(applicationContext)
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .components {
                    add(AppIconFetcher.Factory())
                }
                .build()
        )
    }

}