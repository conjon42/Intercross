package org.phenoapps.intercross.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.sql.Date

@Parcelize
@Entity(tableName = "events")
data class Events(
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = true)
        var id: Long?, var eventDbId: String, var eventName: String,
        var femaleObsUnitDbId: String, var maleOBsUnitDbId: String, var eventValue: Int?,
        var timestamp: String?, var person: String?): Parcelable {

    override fun toString(): String {
        return "$eventDbId,$eventName,$eventValue,$femaleObsUnitDbId,$maleOBsUnitDbId,$person,$timestamp"
    }
}