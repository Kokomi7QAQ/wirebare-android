package top.sankokomi.wirebare.ui.record

import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Stable
@Entity(tableName = HttpDao.REQ_RSP_PAIR_TABLE_NAME)
data class HttpReqRspPair(
    @PrimaryKey
    val id: String,
    val reqId: String,
    val rspId: String
): Parcelable
