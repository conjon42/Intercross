package org.phenoapps.intercross.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings (

    var isPattern: Boolean = false,
    var isUUID: Boolean = false,
    var startFrom: Boolean = false,
    var isAutoIncrement: Boolean = false,
    var pad: Int = 0,
    var number: Int = 0,
    var prefix: String = String(),
    var suffix: String = String(),
    var allowBlank: Boolean = false,
    var order: Int = 0,
    var collectData: Boolean = true
) {

    @ColumnInfo(name = "sid")
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    @Ignore
    val pattern = prefix + number.toString().padStart(pad, '0') + suffix
}
