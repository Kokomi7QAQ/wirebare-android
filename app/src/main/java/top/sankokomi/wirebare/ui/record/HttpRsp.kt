package top.sankokomi.wirebare.ui.record

import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize
import top.sankokomi.wirebare.kernel.interceptor.http.HttpResponse

@Parcelize
@Stable
@Entity(tableName = HttpDao.RSP_TABLE_NAME)
class HttpRsp(
    @PrimaryKey
    val id: String,
    val requestTime: Long?,
    val sourcePort: Short?,
    val destinationAddress: String?,
    val destinationPort: Short?,
    val url: String?,
    val isHttps: Boolean?,
    val httpVersion: String?,
    val rspStatus: String?,
    val originHead: String?,
    @field:TypeConverters(RoomTypeConverter::class)
    val formatHead: List<String>?,
    val host: String?,
    val contentType: String?,
    val contentEncoding: String?,
) : Parcelable {

    companion object {
        fun from(response: HttpResponse): HttpRsp {
            return HttpRsp(
                id = response.id,
                requestTime = response.requestTime,
                sourcePort = response.sourcePort,
                destinationAddress = response.destinationAddress,
                destinationPort = response.destinationPort,
                url = response.url,
                isHttps = response.isHttps,
                httpVersion = response.httpVersion,
                rspStatus = response.rspStatus,
                originHead = response.originHead,
                formatHead = response.formatHead,
                host = response.host,
                contentType = response.contentType,
                contentEncoding = response.contentEncoding,
            )
        }
    }

}
