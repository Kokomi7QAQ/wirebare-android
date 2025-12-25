package top.sankokomi.wirebare.ui.record

import android.graphics.BitmapFactory
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.kernel.util.uncompressBrotli
import top.sankokomi.wirebare.kernel.util.uncompressGzip
import top.sankokomi.wirebare.ui.R
import top.sankokomi.wirebare.ui.resources.RealColumn
import top.sankokomi.wirebare.ui.resources.Typographies
import top.sankokomi.wirebare.ui.util.showToast

@Stable
interface HttpBodyDecompressor {
    suspend fun decompress(id: String): ByteArray
}

@Stable
interface HttpBodyFormatter {
    @Composable
    fun FormatViewer(bytes: ByteArray)
}

suspend fun readHttpBytesById(id: String): ByteArray {
    return withContext(Dispatchers.IO) {
        try {
            val origin = getHttpRecordFileById(id)
            if (origin.exists()) {
                return@withContext origin.readBytes()
            }
        } catch (_: Exception) {
        }
        return@withContext byteArrayOf()
    }
}

fun ByteArray.httpBody(): ByteArray {
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
        return byteArrayOf()
    }
}

object NoneHttpBodyDecompressor : HttpBodyDecompressor {
    override suspend fun decompress(id: String): ByteArray {
        return readHttpBytesById(id).httpBody()
    }

    override fun equals(other: Any?): Boolean {
        return other is NoneHttpBodyDecompressor
    }
}

object GzipHttpBodyDecompressor : HttpBodyDecompressor {
    override suspend fun decompress(id: String): ByteArray {
        try {
            return readHttpBytesById(id).httpBody().uncompressGzip()
        } catch (_: Exception) {
        }
        return byteArrayOf()
    }

    override fun equals(other: Any?): Boolean {
        return other is GzipHttpBodyDecompressor
    }
}

object BrotliHttpBodyDecompressor : HttpBodyDecompressor {
    override suspend fun decompress(id: String): ByteArray {
        try {
            return readHttpBytesById(id).httpBody().uncompressBrotli()
        } catch (_: Exception) {
        }
        return byteArrayOf()
    }

    override fun equals(other: Any?): Boolean {
        return other is BrotliHttpBodyDecompressor
    }
}

object NoneHttpBodyFormatter : HttpBodyFormatter {
    @Composable
    override fun FormatViewer(bytes: ByteArray) {
    }

    override fun equals(other: Any?): Boolean {
        return other is NoneHttpBodyFormatter
    }
}

object TextHttpBodyFormatter : HttpBodyFormatter {
    @Composable
    override fun FormatViewer(bytes: ByteArray) {
        var text by remember { mutableStateOf("") }
        LaunchedEffect(bytes.hashCode()) {
            text = String(bytes, 0, bytes.size)
        }
        if (text.isNotBlank()) {
            RealColumn(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = text,
                    style = Typographies.bodyMedium
                )
            }
        }
    }
}

object HtmlHttpBodyFormatter : HttpBodyFormatter {
    @Composable
    override fun FormatViewer(bytes: ByteArray) {
        var html by remember { mutableStateOf("") }
        LaunchedEffect(bytes.hashCode()) {
            html = String(bytes, 0, bytes.size)
        }
        if (html.isNotBlank()) {
            AndroidView(
                factory = { WebView(it) },
                modifier = Modifier.fillMaxSize(),
                update = { web ->
                    web.webViewClient = WebViewClient()
                    web.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                }
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is HtmlHttpBodyFormatter
    }
}

object ImageHttpBodyFormatter : HttpBodyFormatter {
    @Composable
    override fun FormatViewer(bytes: ByteArray) {
        val defaultImage = remember { createBitmap(1, 1) }
        var image by remember { mutableStateOf(defaultImage) }
        LaunchedEffect(bytes.hashCode()) {
            if (bytes.isEmpty()) {
                image = defaultImage
                return@LaunchedEffect
            }
            val img = withContext(Dispatchers.Default) {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
            if (img != null) {
                image = img
            } else {
                image = defaultImage
                showToast(R.string.req_rsp_info_image_error)
            }
        }
        AsyncImage(
            model = image,
            modifier = Modifier.fillMaxSize(),
            contentDescription = null
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is ImageHttpBodyFormatter
    }
}
