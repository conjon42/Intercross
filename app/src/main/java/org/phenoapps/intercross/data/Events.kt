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

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var date = LocalDateTime.now().format(DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss.SSS"
    ))

    var flowers = 0
    var fruits = 0
    var seeds = 0

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString()) {
        id = parcel.readInt()
        date = parcel.readString()
        flowers = parcel.readInt()
        fruits = parcel.readInt()
        seeds = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(eid)
        parcel.writeString(eventDbId)
        parcel.writeInt(eventValue)
        parcel.writeString(femaleObsUnitDbId)
        parcel.writeString(maleOBsUnitDbId)
        parcel.writeInt(id)
        parcel.writeString(date)
        parcel.writeInt(flowers)
        parcel.writeInt(fruits)
        parcel.writeInt(seeds)
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