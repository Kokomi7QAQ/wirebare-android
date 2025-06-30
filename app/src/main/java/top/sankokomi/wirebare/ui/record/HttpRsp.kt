package top.sankokomi.wirebare.ui.record

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = HttpDao.RSP_TABLE_NAME)
data class HttpRsp(
    @PrimaryKey val id: String,
    val requestTime: Long?,
    val sourcePort: Short?,
    val destinationAddress: String?,
    val destinationPort: Short?,
    val url: String?,
    val isHttps: Boolean?,
    val httpVersion: String?,
    val rspStatus: String?,
    val originHead: String?,
    val host: String?,
    val contentType: String?,
    val contentEncoding: String?
)
