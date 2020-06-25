package org.phenoapps.intercross.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist")
data class Wishlist(var femaleDbId: String,
                    var maleDbId: String,
                    var femaleName: String=femaleDbId,
                    var maleName: String=maleDbId,
                    var wishType: String,
                    var wishCurrent: Int=0,
                    var wishMin: Int,
                    var wishMax: Int?): BaseTable() {

    @ColumnInfo(name = "wid")
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    constructor(row: Array<String>) : this(row[0], row[1],
            row[2], row[3],
            row[4], 0, row[5].toIntOrNull() ?: 0,
            row[6].toIntOrNull() ?: 0)


    companion object {

        val headers = "femaleDbId,maleDbId,femaleName,maleName,wishType,wishCurrent,wishMin,wishMax"
    }
}
