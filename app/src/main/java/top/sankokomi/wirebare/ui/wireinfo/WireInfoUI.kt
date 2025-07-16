package top.sankokomi.wirebare.ui.wireinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.IntentCompat
import top.sankokomi.wirebare.ui.record.HttpReq
import top.sankokomi.wirebare.ui.record.HttpRsp
import top.sankokomi.wirebare.ui.resources.Colors
import top.sankokomi.wirebare.ui.resources.WirebareUITheme

class WireInfoUI : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var request = IntentCompat.getParcelableExtra(intent, "request", HttpReq::class.java)
        var response = IntentCompat.getParcelableExtra(intent, "response", HttpRsp::class.java)
        val sessionId = intent.getStringExtra("session_id")
        enableEdgeToEdge()
        setContent {
            WirebareUITheme(
                statusBarColor = { it.background },
                navigationBarColor = { it.background }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Colors.background
                ) {
                    WireInfoUIPage(
                        sessionId = sessionId ?: "",
                        req = request,
                        rsp = response
                    )
                }
            }
        }
    }

}
