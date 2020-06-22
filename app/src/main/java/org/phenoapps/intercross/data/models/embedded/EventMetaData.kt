package org.phenoapps.intercross.data.models.embedded

import androidx.room.ColumnInfo

data class EventMetaData(

        @ColumnInfo(name = "fruits")
        var fruits: Int,

        @ColumnInfo(name = "seeds")
        var seeds: Int,

        @ColumnInfo(name = "flowers")
        var flowers: Int
)