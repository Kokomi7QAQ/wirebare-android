package top.sankokomi.wirebare.ui.record

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize
import top.sankokomi.wirebare.kernel.interceptor.http.HttpResponse

@Parcelize
@Entity(tableName = HttpDao.RSP_TABLE_NAME)
class HttpRsp(
    @PrimaryKey
    var id: String,
    var requestTime: Long?,
    var sourcePort: Short?,
    var destinationAddress: String?,
    var destinationPort: Short?,
    var url: String?,
    var isHttps: Boolean?,
    var httpVersion: String?,
    var rspStatus: String?,
    var originHead: String?,
    @field:TypeConverters(RoomTypeConverter::class)
    var formatHead: List<String>?,
    var host: String?,
    var contentType: String?,
    var contentEncoding: String?,
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
