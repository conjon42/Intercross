package org.phenoapps.intercross.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.phenoapps.intercross.data.models.Wishlist

@Dao
interface WishlistDao : BaseDao<Wishlist> {

    data class CrossBlock(val maleName: String, val femaleName: String, val count: Int, val min: Int, val max: Int, val pre: Int)

    @Query("""
        SELECT DISTINCT W.maleName as maleName, W.femaleName as femaleName,
            (SELECT COUNT(*)
            FROM events as E
            WHERE E.codeId == W.maleDbId
                and E.codeId == W.femaleDbId) as count, W.wishMin as min,
                W.wishMax as max, W.wishCurrent as pre
        FROM wishlist as W""")
    fun getCrossBlock(): LiveData<List<CrossBlock>>

    @Query("SELECT * FROM wishlist ORDER BY wishlist.wishCurrent DESC")
    fun getAll(): LiveData<List<Wishlist>>

    @Query("DELETE FROM wishlist")
    fun drop()
}