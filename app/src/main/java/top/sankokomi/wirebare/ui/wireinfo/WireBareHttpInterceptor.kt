package top.sankokomi.wirebare.ui.wireinfo

import top.sankokomi.wirebare.kernel.interceptor.http.HttpRequest
import top.sankokomi.wirebare.kernel.interceptor.http.HttpResponse
import top.sankokomi.wirebare.kernel.interceptor.http.HttpSession
import top.sankokomi.wirebare.kernel.interceptor.http.async.AsyncHttpIndexedInterceptor
import top.sankokomi.wirebare.kernel.interceptor.http.async.AsyncHttpInterceptChain
import top.sankokomi.wirebare.kernel.interceptor.http.async.AsyncHttpInterceptor
import top.sankokomi.wirebare.kernel.interceptor.http.async.AsyncHttpInterceptorFactory
import top.sankokomi.wirebare.ui.record.HttpRecorder
import java.nio.ByteBuffer

class WireBareHttpInterceptor(
    private val onRequest: (HttpRequest) -> Unit,
    private val onResponse: (HttpResponse) -> Unit
) : AsyncHttpIndexedInterceptor() {

    class Factory(
        private val onRequest: (HttpRequest) -> Unit,
        private val onResponse: (HttpResponse) -> Unit
    ) : AsyncHttpInterceptorFactory {
        override fun create(): AsyncHttpInterceptor {
            return WireBareHttpInterceptor(onRequest, onResponse)
        }
    }

    override suspend fun onRequest(
        chain: AsyncHttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        index: Int
    ) {
        if (index == 0) {
            HttpRecorder.addRequestRecord(session.request)
            onRequest(session.request)
        }
        HttpRecorder.saveRequestBytes(session.request, buffer)
        super.onRequest(chain, buffer, session, index)
    }

    override suspend fun onRequestFinished(
        chain: AsyncHttpInterceptChain,
        session: HttpSession,
        index: Int
    ) {
        HttpRecorder.saveRequestBytes(session.request, null)
        super.onRequestFinished(chain, session, index)
    }

    override suspend fun onResponse(
        chain: AsyncHttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        index: Int
    ) {
        if (index == 0) {
            HttpRecorder.addResponseRecord(session.response)
            HttpRecorder.addReqRspPairRecord(session)
            onResponse(session.response)
        }
        HttpRecorder.saveResponseBytes(session.response, buffer)
        super.onResponse(chain, buffer, session, index)
    }

    override suspend fun onResponseFinished(
        chain: AsyncHttpInterceptChain,
        session: HttpSession,
        index: Int
    ) {
        HttpRecorder.saveResponseBytes(session.response, null)
        super.onResponseFinished(chain, session, index)
    }
}