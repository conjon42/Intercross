package org.phenoapps.intercross.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PollenDao {
    @Query("SELECT * FROM pollen")
    fun getAll(): LiveData<List<Pollen>>

    @Update
    fun update(vararg e: Pollen?): Int

    @Insert
    fun insert(e: Pollen): Long

    @Delete
    fun delete(vararg e: Pollen?): Int
}