package org.phenoapps.intercross.data

import androidx.room.TypeConverter
import org.phenoapps.intercross.data.models.CrossType

class Converters {

    @TypeConverter
    fun intToCrossType(value: Int): CrossType {

        return when (value) {
            0 -> CrossType.BIPARENTAL
            1 -> CrossType.SELF
            2 -> CrossType.OPEN
            3 -> CrossType.POLY
            else -> CrossType.UNKNOWN
        }
    }

    @TypeConverter
    fun crossTypeToInt(type: CrossType): Int {

        return when (type) {
            CrossType.BIPARENTAL -> 0
            CrossType.SELF -> 1
            CrossType.OPEN -> 2
            CrossType.POLY -> 3
            else -> 4
        }
    }
}