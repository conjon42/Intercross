package org.phenoapps.intercross.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist")
    fun getAll(): LiveData<List<Wishlist>>

    @Update
    fun update(vararg e: Wishlist?): Int

    @Insert
    fun insert(e: Wishlist): Long

    @Query("DELETE FROM wishlist")
    fun deleteAll()

    @Delete
    fun delete(vararg w: Wishlist?): Int
}