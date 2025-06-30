package top.sankokomi.wirebare.ui.record

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = HttpDao.REQ_TABLE_NAME)
data class HttpReq(
    @PrimaryKey val id: String,
    val requestTime: Long?,
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
    val url: String?
)
