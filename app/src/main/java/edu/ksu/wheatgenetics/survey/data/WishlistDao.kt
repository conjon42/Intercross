package edu.ksu.wheatgenetics.survey.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist")
    fun getAll(): LiveData<List<Wishlist>>

    @Update
    fun update(vararg e: Wishlist?): Int

    @Insert
    fun insert(e: Wishlist): Long
}