package org.phenoapps.intercross.data.fts.tables

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "crosses_fts")
@Fts4(contentEntity = Crosses::class)
data class CrossesFts(
    val crossId: String,
    val femaleId: String,
    val maleId: String,
    val femaleName: String?,
    val maleName: String?,
    val date: String,
)