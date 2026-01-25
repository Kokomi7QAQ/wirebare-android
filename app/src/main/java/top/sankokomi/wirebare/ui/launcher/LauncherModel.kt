package top.sankokomi.wirebare.ui.launcher

import top.sankokomi.wirebare.kernel.common.BandwidthLimiter
import top.sankokomi.wirebare.kernel.common.WireBare
import top.sankokomi.wirebare.kernel.interceptor.http.HttpRequest
import top.sankokomi.wirebare.kernel.interceptor.http.HttpResponse
import top.sankokomi.wirebare.kernel.ssl.JKS
import top.sankokomi.wirebare.kernel.util.WireBareLogger
import top.sankokomi.wirebare.ui.datastore.KnetPolicyDataStore
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.util.Global
import top.sankokomi.wirebare.ui.wireinfo.WireBareHttpInterceptor

object LauncherModel {

    const val MTU = 1400
    const val BANDWIDTH_LIMIT_TIMEOUT = 10000L

    val wireBareJKS by lazy {
        JKS(
            jksStream = { Global.appContext.assets.open("wirebare.jks") },
            alias = "wirebare",
            password = "wirebare".toCharArray(),
            algorithm = "RSA",
            type = "PKCS12",
            organization = "WB",
            organizationUnit = "WB"
        )
    }

    fun startProxy(
        targetPackageNameArray: Array<String>,
        onRequest: (HttpRequest) -> Unit,
        onResponse: (HttpResponse) -> Unit
    ) {
        WireBare.logLevel = WireBareLogger.Level.VERBOSE
        WireBare.startProxy {
            if (ProxyPolicyDataStore.enableSSL.value) {
                jks = wireBareJKS
            }
            mtu = MTU
            tcpProxyServerCount = 5
            ipv4ProxyAddress = "10.1.10.1" to 32
            enableIPv6 = ProxyPolicyDataStore.enableIPv6.value
            ipv6ProxyAddress = "a:a:1:1:a:a:1:1" to 128
            addRoutes("0.0.0.0" to 0, "::" to 0)
            addAllowedApplications(*targetPackageNameArray)
            addAsyncHttpInterceptor(
                listOf(
                    WireBareHttpInterceptor.Factory(onRequest, onResponse)
                )
            )

            WireBare.dynamicConfig.bandwidthStatInterval = 2000L
            val maxBandwidthLimit = 2L * MTU
            val reqBandwidthLimit = KnetPolicyDataStore.reqBandwidthLimit.value
            val rspBandwidthLimit = KnetPolicyDataStore.rspBandwidthLimit.value
            WireBare.dynamicConfig.reqBandwidthLimiter = BandwidthLimiter(
                if (reqBandwidthLimit in 1..<maxBandwidthLimit) reqBandwidthLimit else 0L,
                BANDWIDTH_LIMIT_TIMEOUT
            )
            WireBare.dynamicConfig.rspBandwidthLimiter = BandwidthLimiter(
                if (rspBandwidthLimit in 1..<maxBandwidthLimit) rspBandwidthLimit else 0L,
                BANDWIDTH_LIMIT_TIMEOUT
            )
        }
    }

}