package org.phenoapps.intercross.data

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
@Entity(tableName = "events")
data class Events(
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = true)
        var id: Long?, var eventDbId: String, var eventName: String,
        var femaleObsUnitDbId: String, var maleOBsUnitDbId: String, var eventValue: Int?,
        var timestamp: String?, var person: String?, var experiment: String?, var isPoly: Boolean? = false): Parcelable {

    @Transient
    var isSelected: Boolean = false

    override fun toString(): String {
        return "$eventDbId,$eventName,$eventValue,$femaleObsUnitDbId,$maleOBsUnitDbId,$person,$timestamp,$experiment"
    }
}