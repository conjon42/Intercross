package org.phenoapps.intercross.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import org.phenoapps.intercross.data.models.Wishlist
import org.phenoapps.intercross.data.models.WishlistView

@Dao
interface WishlistDao : BaseDao<Wishlist> {

    @Query("SELECT * FROM wishlist ORDER BY wishlist.wishMin DESC")
    fun getAll(): LiveData<List<Wishlist>>

    @Query("SELECT * FROM wishlistView ORDER BY wishlistView.wishProgress DESC")
    fun getAllCounts(): LiveData<List<WishlistView>>

    @Transaction
    @Query("DELETE FROM wishlist")
    fun drop()
}