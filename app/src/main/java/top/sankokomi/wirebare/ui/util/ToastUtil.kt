package top.sankokomi.wirebare.ui.util

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes
import java.lang.ref.WeakReference

private var toastRef: WeakReference<Toast?>? = null

fun showToast(msg: String, time: Int = Toast.LENGTH_SHORT) {
    toastRef?.get()?.cancel()
    if (Looper.getMainLooper() == Looper.myLooper()) {
        val toast = Toast.makeText(Global.appContext, msg, time)
        toastRef = WeakReference(toast)
        toast.show()
    } else {
        Handler(Looper.getMainLooper()).post {
            val toast = Toast.makeText(Global.appContext, msg, time)
            toastRef = WeakReference(toast)
            toast.show()
        }
    }
}

fun showToast(@StringRes stringRes: Int, time: Int = Toast.LENGTH_SHORT) {
    showToast(Global.appContext.resources.getString(stringRes), time)
}
