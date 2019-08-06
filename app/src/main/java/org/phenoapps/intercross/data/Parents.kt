package org.phenoapps.intercross.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parents")
data class Parents(var parentDbId: String, var parentName: String,
                   var parentType: String, var order: String) {
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var isSelected: Boolean = true
}
