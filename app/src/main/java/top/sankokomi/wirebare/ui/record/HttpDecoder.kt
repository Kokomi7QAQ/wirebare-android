package top.sankokomi.wirebare.ui.record

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.kernel.util.unzipBrotli
import top.sankokomi.wirebare.kernel.util.unzipGzip

private const val TAG = "HttpDecoder"

suspend fun decodeHttpBody(id: String): ByteArray? {
    return withContext(Dispatchers.IO) {
        try {
            val origin = getHttpRecordFileById(id)
            if (!origin.exists()) {
                return@withContext null
            } else {
                return@withContext origin.readBytes().httpBody()
            }
        } catch (e: Exception) {
            Log.e(TAG, "decodeHttpBody FAILED", e)
            return@withContext null
        }
    }
}

suspend fun decodeGzipHttpBody(id: String): ByteArray? {
    return withContext(Dispatchers.IO) {
        try {
            return@withContext decodeHttpBody(id)?.unzipGzip()
        } catch (e: Exception) {
            Log.e(TAG, "decodeGzipHttpBody FAILED", e)
            return@withContext null
        }
    }
}

suspend fun decodeBrotliHttpBody(id: String): ByteArray? {
    return withContext(Dispatchers.IO) {
        try {
            return@withContext decodeHttpBody(id)?.unzipBrotli()
        } catch (e: Exception) {
            Log.e(TAG, "decodeBrotliHttpBody FAILED", e)
            return@withContext null
        }
    }
}

suspend fun decodeBitmap(id: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val body = decodeHttpBody(id) ?: return@withContext null
            return@withContext BitmapFactory.decodeByteArray(body, 0, body.size)
        } catch (e: Exception) {
            Log.e(TAG, "decodeBitmap FAILED", e)
            return@withContext null
        }
    }
}

suspend fun decodeGzipBitmap(id: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val body = decodeGzipHttpBody(id) ?: return@withContext null
            return@withContext BitmapFactory.decodeByteArray(body, 0, body.size)
        } catch (e: Exception) {
            Log.e(TAG, "decodeGzipBitmap FAILED", e)
            return@withContext null
        }
    }
}

suspend fun decodeBrotliBitmap(id: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val body = decodeBrotliHttpBody(id) ?: return@withContext null
            return@withContext BitmapFactory.decodeByteArray(body, 0, body.size)
        } catch (e: Exception) {
            Log.e(TAG, "decodeBrotliBitmap FAILED", e)
            return@withContext null
        }
    }
}

private fun ByteArray.httpBody(): ByteArray? {
    try {
        var i = -1
        for (index in 0..size - 4) {
            if (
                this[index] == '\r'.code.toByte() &&
                this[index + 1] == '\n'.code.toByte() &&
                this[index + 2] == '\r'.code.toByte() &&
                this[index + 3] == '\n'.code.toByte()
            ) {
                i = index + 4
                break
            }
        }
        return copyOfRange(i, size)
    } catch (_: Exception) {
        return null
    }
}