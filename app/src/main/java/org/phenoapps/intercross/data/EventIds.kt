package org.phenoapps.intercross.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventids")
data class EventIds(var name: String, var type: String): Parcelable {
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
        id = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(type)
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EventIds> {
        override fun createFromParcel(parcel: Parcel): EventIds {
            return EventIds(parcel)
        }

        override fun newArray(size: Int): Array<EventIds?> {
            return arrayOfNulls(size)
        }
    }

}
