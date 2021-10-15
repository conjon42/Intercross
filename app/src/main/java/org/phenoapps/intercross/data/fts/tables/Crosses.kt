package org.phenoapps.intercross.data.fts.tables

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crosses")
data class Crosses(
    @PrimaryKey
    val rowid: Long,
    val crossId: String,
    val femaleId: String,
    val maleId: String,
    val femaleName: String?,
    val maleName: String?,
    val date: String,
)