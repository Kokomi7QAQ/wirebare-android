package top.sankokomi.wirebare.ui.wireinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.WirebareUITheme

class WireDetailUI : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionId = intent.getStringExtra("session_id") ?: ""
        val decompressTypeCode =
            intent.getIntExtra("decompress_type_code", DecompressType.None.code)
        val contentTypeCode =
            intent.getIntExtra("content_type_code", ContentType.None.code)
        enableEdgeToEdge()
        setContent {
            WirebareUITheme(
                isShowStatusBar = true,
                isShowNavigationBar = false,
                statusBarColor = { it.background }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Colors.background
                ) {
                    LoadDetail(
                        sessionId = sessionId,
                        decompressTypeCode = decompressTypeCode,
                        contentTypeCode = contentTypeCode
                    )
                }
            }
        }
    }

}
