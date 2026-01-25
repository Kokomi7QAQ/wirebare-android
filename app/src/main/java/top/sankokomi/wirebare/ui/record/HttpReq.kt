package top.sankokomi.wirebare.ui.record

import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize
import top.sankokomi.wirebare.kernel.interceptor.http.HttpSession

@Parcelize
@Stable
@Entity(tableName = HttpDao.REQ_TABLE_NAME)
class HttpReq(
    @PrimaryKey
    val id: String,
    val requestTime: Long?,
    val sourceProcessUid: Int,
    val sourcePort: Short?,
    val sourcePkgName: String?,
    val destinationAddress: String?,
    val destinationPort: Short?,
    val method: String?,
    val isHttps: Boolean?,
    val httpVersion: String?,
    val host: String?,
    val path: String?,
    val originHead: String?,
    @field:TypeConverters(RoomTypeConverter::class)
    val formatHead: List<String>?,
    val url: String?,
) : Parcelable {

    companion object {
        fun from(session: HttpSession): HttpReq {
            val request = session.request
            return HttpReq(
                id = request.id,
                requestTime = request.requestTime,
                sourceProcessUid = session.tcpSession.sourceProcessUid,
                sourcePort = request.sourcePort,
                sourcePkgName = request.sourcePkgName,
                destinationAddress = request.destinationAddress,
                destinationPort = request.destinationPort,
                method = request.method,
                isHttps = request.isHttps,
                httpVersion = request.httpVersion,
                host = request.host,
                path = request.path,
                originHead = request.originHead,
                formatHead = request.formatHead,
                url = request.url,
            )
        }
    }

}
