package top.sankokomi.wirebare.ui.record

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.kernel.interceptor.http.HttpRequest
import top.sankokomi.wirebare.kernel.interceptor.http.HttpResponse
import top.sankokomi.wirebare.ui.util.Global
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

private val recordDir by lazy {
    File("${Global.appContext.externalCacheDir!!.absolutePath}${File.separator}http_record").also {
        if (!it.exists()) it.mkdirs()
    }
}

val HttpRequest.id: String get() = "req|${requestTime}|${sequence}"

val HttpResponse.id: String get() = "rsp|${requestTime}|${sequence}"

fun getHttpRecordFileById(id: String): File = File(recordDir, id)

object HttpRecorder {

    private val writers = ConcurrentHashMap<String, ConcurrentFileWriter>()

    fun parseRequestRecordFile(request: HttpRequest): File {
        val dest = File(recordDir, request.id)
        if (!dest.exists()) {
            dest.createNewFile()
        }
        return dest
    }

    fun parseResponseRecordFile(response: HttpResponse): File {
        val dest = File(recordDir, response.id)
        if (!dest.exists()) {
            dest.createNewFile()
        }
        return dest
    }

    suspend fun queryRequestRecord(): List<HttpReq> {
        return withContext(Dispatchers.IO) {
            httpRoom.httpDao().queryHttpReqList()
        }
    }

    suspend fun queryResponseRecord(): List<HttpRsp> {
        return withContext(Dispatchers.IO) {
            httpRoom.httpDao().queryHttpRspList()
        }
    }

    suspend fun addRequestRecord(request: HttpRequest, buffer: ByteBuffer?) {
        withContext(Dispatchers.IO) {
            try {
                val req = HttpReq.from(request)
                httpRoom.httpDao().insertHttpReq(listOf(req))
                val id = req.id
                if (buffer == null) {
                    writers.remove(id)?.close()
                    return@withContext
                }
                writers.computeIfAbsent(id) {
                    ConcurrentFileWriter(parseRequestRecordFile(request))
                }.writeBytes(buffer)
            } catch (_: Exception) {
            }
        }
    }

    suspend fun addResponseRecord(response: HttpResponse, buffer: ByteBuffer?) {
        withContext(Dispatchers.IO) {
            try {
                val rsp = HttpRsp.from(response)
                httpRoom.httpDao().insertHttpRsp(listOf(rsp))
                val id = rsp.id
                if (buffer == null) {
                    writers.remove(id)?.close()
                    return@withContext
                }
                writers.computeIfAbsent(id) {
                    ConcurrentFileWriter(parseResponseRecordFile(response))
                }.writeBytes(buffer)
            } catch (_: Exception) {
            }
        }
    }

    suspend fun clearReqRecord() {
        withContext(Dispatchers.IO) {
            try {
                httpRoom.httpDao().clearHttpReq()
                recordDir.listFiles()?.filter { it.name.startsWith("req") }?.forEach(File::delete)
            } catch (_: Exception) {
            }
        }
    }

    suspend fun clearRspRecord() {
        withContext(Dispatchers.IO) {
            try {
                httpRoom.httpDao().clearHttpRsp()
                recordDir.listFiles()?.filter { it.name.startsWith("rsp") }?.forEach(File::delete)
            } catch (_: Exception) {
            }
        }
    }

}