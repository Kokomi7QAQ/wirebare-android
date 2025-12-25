package top.sankokomi.wirebare.ui.datastore

object ProxyPolicyDataStore : AppDataStore("proxy_policy") {

    /**
     * 记录首页 tab 位置
     * */
    val mainTabIndex by AppIntPref("main_tab_index", 2)

    /**
     * true：禁用自动过滤，false：启用自动过滤
     * */
    val banAutoFilter by AppBooleanPref("ban_auto_filter")

    /**
     * true：启用 SSL，false：禁用 SSL
     * */
    val enableSSL by AppBooleanPref("enable_ssl")

    /**
     * true：启用 ipv6，false：禁用 ipv6
     * */
    val enableIPv6 by AppBooleanPref("enable_ipv6")

    /**
     * true：启用休眠锁防止休眠，false：禁用休眠锁
     * */
    val enableWakeLock by AppBooleanPref("enable_wake_lock")

    /**
     * true：显示系统应用，false：不显示系统应用
     * */
    val showSystemApp by AppBooleanPref("ban_system_app")
}