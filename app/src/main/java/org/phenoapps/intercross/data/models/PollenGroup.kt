package org.phenoapps.intercross.data.models

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/***
 * The Pollen Group table is used to group males into a single bar code id.
 *
 * Pollen groups can reference the Events table and the Parents table.
 */
@Keep
@Entity(tableName = "pollen_groups")
data class PollenGroup(

        @ColumnInfo(name = "codeId")
        var codeId: String,

        @ColumnInfo(name = "name")
        var name: String,

        @ColumnInfo(name = "maleId")
        var maleId: Long?,

        @ColumnInfo(name = "gid")
        @PrimaryKey(autoGenerate = true)
        var id: Long? = null) {

        var selected: Boolean = false
}