package top.sankokomi.wirebare.ui.record

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import top.sankokomi.wirebare.ui.util.Global

@Database(version = 1, entities = [HttpReq::class, HttpRsp::class])
abstract class HttpDatabase : RoomDatabase() {

    companion object {
        const val DB_NAME = "http_data_base"

        val room by lazy {
            Room.databaseBuilder<HttpDatabase>(Global.appContext, DB_NAME).build()
        }
    }

    abstract fun httpDao(): HttpDao
}

@Dao
interface HttpDao {

    companion object {
        const val REQ_TABLE_NAME = "http_req"
        const val RSP_TABLE_NAME = "http_rsp"
    }

    @Insert(entity = HttpReq::class)
    fun insertHttpReq(reqList: List<HttpReq>)

    @Query("SELECT * FROM $REQ_TABLE_NAME ORDER BY requestTime")
    fun queryAllHttpReq(): List<HttpReq>

    @Insert(entity = HttpReq::class)
    fun insertHttpRsp(reqList: List<HttpRsp>)

    @Query("SELECT * FROM $RSP_TABLE_NAME ORDER BY requestTime")
    fun queryAllHttpRsp(): List<HttpRsp>

}