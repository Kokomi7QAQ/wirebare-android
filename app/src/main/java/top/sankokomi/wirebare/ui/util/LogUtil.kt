package top.sankokomi.wirebare.ui.util

import android.util.Log

object LogUtil {

    fun v(tag: String?, msg: String?, tr: Throwable? = null) {
        Log.v(tag, msg, tr)
    }

    fun d(tag: String?, msg: String?, tr: Throwable? = null) {
        Log.d(tag, msg, tr)
    }

    fun i(tag: String?, msg: String?, tr: Throwable? = null) {
        Log.i(tag, msg, tr)
    }

    fun w(tag: String?, msg: String?, tr: Throwable? = null) {
        Log.w(tag, msg, tr)
    }

    fun e(tag: String?, msg: String?, tr: Throwable? = null) {
        Log.e(tag, msg, tr)
    }

    fun wtf(tag: String?, msg: String?, tr: Throwable? = null) {
        Log.wtf(tag, msg, tr)
    }

}