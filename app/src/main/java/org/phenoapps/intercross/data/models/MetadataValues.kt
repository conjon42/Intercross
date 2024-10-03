package org.phenoapps.intercross.data.models

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Metadata values table to hold event metadata values.
 * Such as cross with codeId "uniquePumpkin has 18 seeds"
 */
@Keep
@Entity(tableName = "metaValues",
        indices = [Index(value = ["eid", "metaId"], unique = true)])
data class MetadataValues(

        @ColumnInfo(name = "eid")
        var eid: Int,

        @ColumnInfo(name = "metaId")
        var metaId: Int,

        @ColumnInfo(name = "value")
        var value: Int? = null,

        @ColumnInfo(name = "mvId")
        @PrimaryKey(autoGenerate = true)
        var id: Long? = null): BaseTable()

