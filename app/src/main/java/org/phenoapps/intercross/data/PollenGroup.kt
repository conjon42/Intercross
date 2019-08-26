package org.phenoapps.intercross.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "pollen_groups")
data class PollenGroup(@ColumnInfo(name = "id")
                       @PrimaryKey(autoGenerate = true) var id: Long?,
                       var name: String, var uuid: String): Parcelable