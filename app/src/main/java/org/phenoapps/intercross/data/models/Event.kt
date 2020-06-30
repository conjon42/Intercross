package org.phenoapps.intercross.data.models

import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
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

        @Embedded
        val metaData: EventMetaData = EventMetaData(0, 0, 0),

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
    }

    override fun toString(): String {

        return "$femaleObsUnitDbId,$maleObsUnitDbId,$readableName,$timestamp,$person,$experiment,$type,$sex,${metaData.fruits},${metaData.flowers},${metaData.seeds}"

    }

    fun toPollenGroupString(malesRepr: String): String {

        return "$femaleObsUnitDbId,$malesRepr,$readableName,$timestamp,$person,$experiment,${CrossType.POLY},1,${metaData.fruits},${metaData.flowers},${metaData.seeds}"

    }
}