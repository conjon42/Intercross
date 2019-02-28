package edu.ksu.wheatgenetics.survey.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "samples",
        foreignKeys = [ForeignKey(
        entity = Experiment::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("eid"),
        onDelete = CASCADE
)])
data class Sample(
        var eid: Int,
        var name: String,
        var latitude: Double,
        var longitude: Double,
        var person: String,
        var plot: String
) {
        @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true)
        var id = 0
}