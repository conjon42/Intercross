package org.phenoapps.intercross.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import org.phenoapps.intercross.data.models.Wishlist
import org.phenoapps.intercross.data.models.WishlistView
import org.phenoapps.intercross.fragments.CrossBlockFragment

@Dao
interface WishlistDao : BaseDao<Wishlist> {

    @Query("SELECT * FROM wishlist ORDER BY wishlist.wishMin DESC")
    fun getAll(): LiveData<List<Wishlist>>

    @Query("SELECT * FROM wishlistView ORDER BY wishlistView.wishProgress DESC")
    fun getAllCounts(): LiveData<List<WishlistView>>

    @Query("""
        SELECT DISTINCT femaleDbId as momId, femaleName as momName, maleDbId as dadId, maleName as dadName, wishMin, wishMax, wishType,
            (SELECT COUNT(*) 
            FROM events as child
            WHERE (w.femaleDbId = child.mom and w.maleDbId = child.dad)
                or (w.femaleDbId = child.dad and w.maleDbId = child.mom)) as wishProgress
        FROM wishlist as w
        """)
    fun getAllCommutativeWishCounts(): LiveData<List<WishlistView>>

    @Query("""
        SELECT DISTINCT femaleDbId as momId, femaleName as momName, maleDbId as dadId, maleName as dadName, wishMin, wishMax, wishType,
            (SELECT COUNT(*) 
            FROM events as child
            WHERE (w.femaleDbId = child.mom and w.maleDbId = child.dad)
                or (w.femaleDbId = child.dad and w.maleDbId = child.mom)) as wishProgress
        FROM wishlist as w
        WHERE wishType = 'cross' ORDER BY wishProgress DESC
        """)
    fun getCommutativeCrossBlock(): LiveData<List<WishlistView>>

    @Query("SELECT * FROM wishlistView WHERE wishType = 'cross' ORDER BY wishlistView.wishProgress DESC")
    fun getCrossblock(): LiveData<List<WishlistView>>

    @Query("SELECT DISTINCT dadId as code, dadName as name FROM wishlistView ORDER BY wishlistView.wishProgress DESC")
    fun getMaleHeaders(): LiveData<List<CrossBlockFragment.HeaderData>>

    @Query("SELECT DISTINCT momId as code, momName as name FROM wishlistView ORDER BY wishlistView.wishProgress DESC")
    fun getFemaleHeaders(): LiveData<List<CrossBlockFragment.HeaderData>>

    @Transaction
    @Query("DELETE FROM wishlist")
    fun drop()
}