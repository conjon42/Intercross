package edu.ksu.wheatgenetics.survey.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist")
data class Wishlist(var maleDbId: String, var femaleDbId: String,
                    var maleName: String, var femaleName: String,
                    var wishType: String, var wishMin: Int, var wishMax: Int) {
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
