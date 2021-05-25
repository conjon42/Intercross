package org.phenoapps.intercross.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Parent Table
 *
 *
 * Primary Key is inherited through the open Base Table class
 */
@Entity(tableName = "parents", indices = [Index(value = ["codeId"], unique = false)])
data class Parent(@ColumnInfo(name = "codeId")
                  val codeId: String, var sex: Int): BaseParent() {

    /**
     * Selected column is only used for saving check box states.
     */
    var selected: Boolean = false

    @ColumnInfo(name = "pid")
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    @ColumnInfo(name = "name")
    var name: String = codeId

    var isPoly: Boolean = false

    constructor(codeId: String, sex: Int, name: String?, isPoly: Boolean = false): this(codeId, sex) {

        name?.let { readableName ->

            this.name = readableName

            this.isPoly = isPoly
        }
    }
}

