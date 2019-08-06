package org.phenoapps.intercross.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings (

    @PrimaryKey
    var id: Int = 0,

    var isPattern: Boolean = false,

    var isUUID: Boolean = false,

    var startFrom: Boolean = false,

    var isAutoIncrement: Boolean = false,

    var pad: Int = 0,

    var number: Int = 0,

    var prefix: String = String(),

    var suffix: String = String(),

    var allowBlank: Boolean = false,

    var order: Int = 0
)
