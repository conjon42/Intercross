package org.phenoapps.intercross.data.models

import androidx.recyclerview.widget.DiffUtil
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
@Entity(tableName = "parents", indices = [Index(value = ["name"])])
data class Parent(@ColumnInfo(name = "codeId")
                  val codeId: String, val sex: Int): BaseTable() {

    /**
     * Selected column is only used for saving check box states.
     */
    var selected: Boolean = false

    @ColumnInfo(name = "pid")
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    @ColumnInfo(name = "name")
    var name: String = codeId

    constructor(codeId: String, sex: Int, name: String?): this(codeId, sex) {

        name?.let { readableName ->

            this.name = readableName
        }
    }

    companion object {

        class DiffCallback : DiffUtil.ItemCallback<Parent>() {

            override fun areItemsTheSame(oldItem: Parent, newItem: Parent): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Parent, newItem: Parent): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}

