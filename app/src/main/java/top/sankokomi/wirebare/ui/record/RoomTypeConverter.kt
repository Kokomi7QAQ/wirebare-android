package top.sankokomi.wirebare.ui.record

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

@ProvidedTypeConverter
class RoomTypeConverter {

    @TypeConverter
    fun jsonToStringList(jsonString: String?): List<String>? {
        return Json.decodeFromString(
            ListSerializer(String.serializer()),
            jsonString ?: return null
        )
    }

    @TypeConverter
    fun stringListToJson(stringList: List<String>?): String? {
        return Json.encodeToString(
            ListSerializer(String.serializer()),
            stringList ?: return null
        )
    }

}