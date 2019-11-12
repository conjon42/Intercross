package org.phenoapps.intercross.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PollenGroupDao {
    @Query("SELECT * FROM pollen_groups")
    fun getAll(): LiveData<List<PollenGroup>>

    @Update
    fun update(vararg e: PollenGroup?): Int

    @Insert
    fun insert(e: PollenGroup): Long

    @Delete
    fun delete(vararg e: PollenGroup?): Int
}