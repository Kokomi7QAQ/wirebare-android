package top.sankokomi.wirebare.ui.record

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize
import top.sankokomi.wirebare.kernel.interceptor.http.HttpRequest

@Parcelize
@Entity(tableName = HttpDao.REQ_TABLE_NAME)
class HttpReq(
    @PrimaryKey
    var id: String,
    var requestTime: Long?,
    var sourcePort: Short?,
    var sourcePkgName: String?,
    var destinationAddress: String?,
    var destinationPort: Short?,
    var method: String?,
    var isHttps: Boolean?,
    var httpVersion: String?,
    var host: String?,
    var path: String?,
    var originHead: String?,
    @field:TypeConverters(RoomTypeConverter::class)
    var formatHead: List<String>?,
    var url: String?,
) : Parcelable {

    companion object {
        fun from(request: HttpRequest): HttpReq {
            return HttpReq(
                id = request.id,
                requestTime = request.requestTime,
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
