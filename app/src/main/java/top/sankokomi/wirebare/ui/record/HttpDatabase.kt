package top.sankokomi.wirebare.ui.record

import android.os.Process
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import top.sankokomi.wirebare.ui.record.HttpDatabase.Companion.DB_NAME
import top.sankokomi.wirebare.ui.util.Global

val httpRoom by lazy {
    Room.databaseBuilder<HttpDatabase>(
        Global.appContext, DB_NAME
    ).addTypeConverter(
        RoomTypeConverter()
    ).addMigrations(
        Migration(1, 2) {
            it.execSQL(
                "CREATE TABLE ${HttpDao.REQ_RSP_PAIR_TABLE_NAME}" +
                        " (id TEXT NOT NULL, reqId TEXT NOT NULL, rspId TEXT NOT NULL, PRIMARY KEY(id))"
            )
        }
    ).addMigrations(
        Migration(2, 3) {
            it.execSQL(
                "ALTER TABLE ${HttpDao.REQ_TABLE_NAME}" +
                        " ADD COLUMN sourceProcessUid INTEGER NOT NULL DEFAULT ${Process.INVALID_UID}"
            )
            it.execSQL(
                "ALTER TABLE ${HttpDao.RSP_TABLE_NAME}" +
                        " ADD COLUMN sourceProcessUid INTEGER NOT NULL DEFAULT ${Process.INVALID_UID}"
            )
        }
    ).build()
}

@Database(version = 3, entities = [HttpReq::class, HttpRsp::class, HttpReqRspPair::class])
abstract class HttpDatabase : RoomDatabase() {

    companion object {
        const val DB_NAME = "http_data_base"
    }

    abstract fun httpDao(): HttpDao
}

@Dao
interface HttpDao {

    companion object {
        const val REQ_TABLE_NAME = "http_req"
        const val RSP_TABLE_NAME = "http_rsp"
        const val REQ_RSP_PAIR_TABLE_NAME = "http_req_rsp_pair"
    }

    @Insert(entity = HttpReq::class, onConflict = OnConflictStrategy.IGNORE)
    fun insertHttpReq(req: HttpReq)

    @Query("SELECT * FROM $REQ_TABLE_NAME ORDER BY requestTime")
    fun queryHttpReqList(): List<HttpReq>

    @Query("SELECT * FROM $REQ_TABLE_NAME WHERE id = :reqId")
    fun queryHttpReqById(reqId: String): List<HttpReq>

    @Query("DELETE FROM $REQ_TABLE_NAME")
    fun clearHttpReq()

    @Insert(entity = HttpRsp::class, onConflict = OnConflictStrategy.IGNORE)
    fun insertHttpRsp(req: HttpRsp)

    @Query("SELECT * FROM $RSP_TABLE_NAME ORDER BY requestTime")
    fun queryHttpRspList(): List<HttpRsp>

    @Query("SELECT * FROM $RSP_TABLE_NAME WHERE id = :rspId")
    fun queryHttpRspById(rspId: String): List<HttpRsp>

    @Query("DELETE FROM $RSP_TABLE_NAME")
    fun clearHttpRsp()

    @Insert(entity = HttpReqRspPair::class, onConflict = OnConflictStrategy.IGNORE)
    fun insertReqRspPair(httpReqRspPair: HttpReqRspPair)

    @Query("SELECT rspId FROM $REQ_RSP_PAIR_TABLE_NAME WHERE reqId = :reqId")
    fun queryRspId(reqId: String): String?

    @Query("SELECT reqId FROM $REQ_RSP_PAIR_TABLE_NAME WHERE rspId = :rspId")
    fun queryReqId(rspId: String): String?

}