package top.sankokomi.wirebare.core.ssl

import top.sankokomi.wirebare.core.net.Port
import java.security.KeyStore
import java.security.PrivateKey
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext


class SSLEngineFactory(private val jks: JKS) {

    companion object {
        private const val KEY_STORE_TYPE_JKS = "JKS"
        private const val SSL_PROTOCOL_SSL = "SSL"
        private const val SSL_PROTOCOL_SSL_V2 = "SSLv2"
        private const val SSL_PROTOCOL_SSL_V3 = "SSLv3"
        private const val SSL_PROTOCOL_TLS = "TLS"
        private const val SSL_PROTOCOL_TLS_V1 = "TLSv1"
        private const val SSL_PROTOCOL_TLS_V1_1 = "TLSv1.1"
        private const val SSL_PROTOCOL_TLS_V1_2 = "TLSv1.2"
        private val SSL_PROTOCOLS = arrayOf(
            SSL_PROTOCOL_TLS_V1_2,
//            SSL_PROTOCOL_TLS_V1_1,
            SSL_PROTOCOL_TLS_V1,
//            SSL_PROTOCOL_TLS,
//            SSL_PROTOCOL_SSL_V3,
//            SSL_PROTOCOL_SSL_V2,
//            SSL_PROTOCOL_SSL
        )
    }

    private val keyStore: KeyStore = KeyStore.getInstance(jks.type).also {
        it.load(jks.jksStream(), jks.password)
    }
    private val certificate = keyStore.getCertificate(jks.alias)
    private val privateKey = keyStore.getKey(jks.alias, jks.password) as PrivateKey

    fun createClientSSLEngine(host: String, port: Port): WireBareSSLEngine? {
        val engine = requireSSLContext(host)?.createSSLEngine(
            host, port.port.toInt() and 0xFFFF
        ) ?: return null
        val ciphers = mutableListOf<String>()
        for (cipher in engine.enabledCipherSuites) {
            if (
                cipher != "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" &&
                cipher != "TLS_DHE_RSA_WITH_AES_256_CBC_SHA"
            ) {
                ciphers.add(cipher)
            }
        }
        engine.enabledCipherSuites = ciphers.toTypedArray()
        engine.useClientMode = true
        engine.needClientAuth = false
        return WireBareSSLEngine(engine)
    }

    fun createServerSSLEngine(host: String): WireBareSSLEngine? {
        val engine = requireSSLContext(host)?.createSSLEngine() ?: return null
        engine.useClientMode = false
        engine.wantClientAuth = false
        engine.needClientAuth = false
        return WireBareSSLEngine(engine)
    }

    private val sslContextMap = hashMapOf<String, SSLContext>()

    private fun requireSSLContext(host: String): SSLContext? {
        val cache = sslContextMap[host]
        if (cache != null) {
            return cache
        }
        val context = createSSLContext(host) ?: return null
        sslContextMap[host] = context
        return context
    }

    private fun createSSLContext(host: String): SSLContext? {
        return realCreateSSLContext()?.also { context ->
            val kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
            ).also {
                it.init(
                    CertificateFactory.generateServer(host, jks, certificate, privateKey),
                    jks.password
                )
            }
            context.init(kmf.keyManagers, null, null)
        }
    }

    private fun realCreateSSLContext(): SSLContext? {
        var context: SSLContext? = null
        for (protocol in SSL_PROTOCOLS) {
            kotlin.runCatching {
                context = SSLContext.getInstance(protocol)
            }
            if (context != null) {
                break
            }
        }
        return context
    }

}