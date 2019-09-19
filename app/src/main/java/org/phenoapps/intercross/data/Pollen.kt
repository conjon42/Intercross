package org.phenoapps.intercross.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
@Parcelize
@Entity(tableName = "pollen",
        foreignKeys = [ForeignKey(entity = Events::class,
                parentColumns = ["id"], childColumns = ["eid"]),
        ForeignKey(entity = PollenGroup::class,
                parentColumns = ["id"], childColumns = ["pid"])])
data class Pollen(@ColumnInfo(name = "id")
                  @PrimaryKey(autoGenerate = true) var id: Long?,
                  var pid: Int, var eid: Int): Parcelable {

    @Transient
    var isSelected: Boolean = true
}