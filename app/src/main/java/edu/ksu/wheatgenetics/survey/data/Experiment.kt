package edu.ksu.wheatgenetics.survey.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "experiments")
data class Experiment(var name: String, var count: Int = 0): Parcelable {
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt()) {
        id = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(count)
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Experiment> {
        override fun createFromParcel(parcel: Parcel): Experiment {
            return Experiment(parcel)
        }

        override fun newArray(size: Int): Array<Experiment?> {
            return arrayOfNulls(size)
        }
    }

}
