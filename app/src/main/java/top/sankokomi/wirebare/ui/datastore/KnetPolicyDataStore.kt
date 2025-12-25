package top.sankokomi.wirebare.ui.datastore

import top.sankokomi.wirebare.ui.launcher.LauncherModel

object KnetPolicyDataStore : AppDataStore("knet_policy") {

    /**
     * 请求带宽限额
     * */
    val reqBandwidthLimit by AppLongPref("req_bandwidth_limit", 2L * LauncherModel.MTU)

    /**
     * 响应带宽限额
     * */
    val rspBandwidthLimit by AppLongPref("rsp_bandwidth_limit", 2L * LauncherModel.MTU)

    /**
     * 悬浮窗开关
     * */
    val enableFloatingWindow by AppBooleanPref("enable_floating_window")

    /**
     * 悬浮窗 x 位置
     * */
    val floatingWindowX by AppIntPref("floating_window_x")

    /**
     * 悬浮窗 y 位置
     * */
    val floatingWindowY by AppIntPref("floating_window_y")

    /**
     * 悬浮窗是否只在后台时显示
     * */
    val enableFloatingWindowOnlyInBackground by AppBooleanPref("enable_floating_window_only_in_background")

    /**
     * 悬浮窗小窗模式
     * */
    val enableMiniFloatingWindow by AppBooleanPref("enable_mini_floating_window", false)
}