package org.phenoapps.intercross.data.models

import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
import org.phenoapps.intercross.data.models.embedded.EventMetaData

/**
 * Event is the main table that holds barcode data.
 */
@Keep
@Entity(tableName = "events",
        indices = [Index(value = ["codeId"], unique = true)])
data class Event(

        @ColumnInfo(name = "codeId")
        var eventDbId: String,

        @ColumnInfo(name = "mom")
        var femaleObsUnitDbId: String="?",

        @ColumnInfo(name = "dad")
        var maleObsUnitDbId: String="?",

        @ColumnInfo(name = "name")
        var readableName: String=eventDbId,

        @ColumnInfo(name = "date")
        var timestamp: String="?",

        var person: String="?",

        var experiment: String="?",

        var type: CrossType=CrossType.UNKNOWN,

        var sex: Int = -1, //by default sex is unknown

        var metadata: String = metadataDefault,

        @ColumnInfo(name = "eid")
        @PrimaryKey(autoGenerate = true)
        var id: Long? = null): BaseTable() {

    init {

        /**
         * Poly Crosses are explicitly classified, otherwise implicitly determine cross type.
         */

        if (type != CrossType.POLY) {

            type = when {

                maleObsUnitDbId == "blank" -> CrossType.OPEN

                femaleObsUnitDbId == maleObsUnitDbId -> CrossType.SELF

                else -> CrossType.BIPARENTAL

            }
        }
    }

    companion object {

        class DiffCallback : DiffUtil.ItemCallback<Event>() {

            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem.id== newItem.id
            }

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
                return oldItem.id == newItem.id
            }
        }

        const val metadataDefault = """{"fruits":[0,0], "seeds":[0,0], "flowers":[0,0]}"""
    }

    override fun toString(): String {

        val json = metadata.toJson()

        return "$eventDbId,$femaleObsUnitDbId,$maleObsUnitDbId,$timestamp,$person,$experiment,$type${if (json.isEmpty()) String() else ",$json"}"

    }

    fun toPollenGroupString(malesRepr: String, groupName: String?): String {

        var group = groupName ?: maleObsUnitDbId

        val json = metadata.toJson()

        return "$eventDbId,$femaleObsUnitDbId,$maleObsUnitDbId::$group::$malesRepr,$timestamp,$person,$experiment,${CrossType.POLY},${if (json.isEmpty()) String() else ",$json"}"

    }

    fun getMetadataHeaders(): String = try {

        val element = JsonParser.parseString(metadata)

        if (element.isJsonObject) {

            val json = element.asJsonObject

            if (json.entrySet().isEmpty()) String()
            else json.entrySet().joinToString(",", ",") { it.key }

        } else String()

    } catch (e: JsonSyntaxException) {

        e.printStackTrace()

        String()
    }

    //takes a json string and creates a comma delimited string of its values
    //in this case the json is always an object with json arrays as values with two entries, the value and default value
    private fun String.toJson(): String = try {

        val element = JsonParser.parseString(this)

        if (element.isJsonObject) {

            val json = element.asJsonObject

            if (json.entrySet().isEmpty()) String()
            else json.entrySet().joinToString(",") { it.value.asJsonArray[0].asJsonPrimitive.toString() }

        } else String()

    } catch (e: JsonSyntaxException) {

        e.printStackTrace()

        String()
    }

}