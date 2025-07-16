package top.sankokomi.wirebare.ui.wireinfo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.ui.record.BrotliHttpBodyDecompressor
import top.sankokomi.wirebare.ui.record.GzipHttpBodyDecompressor
import top.sankokomi.wirebare.ui.record.HtmlHttpBodyFormatter
import top.sankokomi.wirebare.ui.record.HttpBodyDecompressor
import top.sankokomi.wirebare.ui.record.HttpBodyFormatter
import top.sankokomi.wirebare.ui.record.ImageHttpBodyFormatter
import top.sankokomi.wirebare.ui.record.NoneHttpBodyDecompressor
import top.sankokomi.wirebare.ui.record.NoneHttpBodyFormatter
import top.sankokomi.wirebare.ui.record.TextHttpBodyFormatter
import top.sankokomi.wirebare.ui.resources.RealBox

enum class DecompressType(val code: Int) {
    None(0),
    Gzip(1),
    Brotli(2);

    companion object {
        fun parse(code: Int): HttpBodyDecompressor {
            return when (code) {
                Gzip.code -> GzipHttpBodyDecompressor
                Brotli.code -> BrotliHttpBodyDecompressor
                else -> NoneHttpBodyDecompressor
            }
        }
    }
}

enum class ContentType(val code: Int) {
    None(0),
    Text(1),
    Html(2),
    Image(3);

    companion object {
        fun parse(code: Int): HttpBodyFormatter {
            return when (code) {
                Text.code -> TextHttpBodyFormatter
                Html.code -> HtmlHttpBodyFormatter
                Image.code -> ImageHttpBodyFormatter
                else -> NoneHttpBodyFormatter
            }
        }
    }
}

@Composable
fun LoadDetail(
    sessionId: String,
    decompressTypeCode: Int,
    contentTypeCode: Int
) {
    RealBox(modifier = Modifier.fillMaxSize()) {
        val decompressor = DecompressType.parse(decompressTypeCode)
        val formater = ContentType.parse(contentTypeCode)
        var bytes by remember { mutableStateOf(byteArrayOf()) }
        LaunchedEffect(decompressor) {
            withContext(Dispatchers.IO) {
                bytes = decompressor.decompress(sessionId)
            }
        }
        formater.FormatViewer(bytes)
    }
}
