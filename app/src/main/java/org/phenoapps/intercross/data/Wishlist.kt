package org.phenoapps.intercross.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist")
data class Wishlist(var femaleDbId: String, var maleDbId: String,
                    var femaleName: String, var maleName: String,
                    var wishType: String, var wishMin: Int, var wishMax: Int) {
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
