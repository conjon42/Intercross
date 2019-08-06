package org.phenoapps.intercross.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "events")
data class Events(var eid: Int, var eventDbId: String, var eventValue: Int,
                  var femaleObsUnitDbId: String, var maleOBsUnitDbId: String): Parcelable {

    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var date = LocalDateTime.now().format(DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss.SSS"
    ))

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString()) {
        id = parcel.readInt()
        date = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(eid)
        parcel.writeString(eventDbId)
        parcel.writeInt(eventValue)
        parcel.writeString(femaleObsUnitDbId)
        parcel.writeString(maleOBsUnitDbId)
        parcel.writeInt(id)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Events> {
        override fun createFromParcel(parcel: Parcel): Events {
            return Events(parcel)
        }

        override fun newArray(size: Int): Array<Events?> {
            return arrayOfNulls(size)
        }
    }
}
/*package org.phenoapps.intercross.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SampleDao {
    @Query("SELECT * FROM samples WHERE :eid = eid")
    fun getAll(eid: Int): LiveData<List<Sample>>

    @Query("SELECT * from samples WHERE :eid = eid and plot = :plot")
    fun getPlot(eid: Int, plot: String): LiveData<List<Sample>>

    @Query("SELECT plot from samples WHERE :eid = eid")
    fun getPlotNames(eid: Int): LiveData<List<String>>

    @Update
    fun updateSamples(vararg s: Sample): Int

    @Insert
    fun insert(s: Sample): Long
}*/