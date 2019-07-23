package edu.ksu.wheatgenetics.survey.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "events")
data class Events(var eid: Int, var eventDbId: String, var eventValue: Int,
                  var femaleObsUnitDbId: String, var maleOBsUnitDbId: String) {

    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var date = LocalDateTime.now().format(DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss.SSS"
    ))
}
