package org.phenoapps.intercross.data.models

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


open class BaseParent: BaseTable() {

    companion object {

        class DiffCallback : DiffUtil.ItemCallback<BaseParent>() {

            override fun areItemsTheSame(oldItem: BaseParent, newItem: BaseParent): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areContentsTheSame(oldItem: BaseParent, newItem: BaseParent): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }
    }
}

