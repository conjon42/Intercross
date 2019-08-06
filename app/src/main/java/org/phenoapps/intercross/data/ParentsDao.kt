package org.phenoapps.intercross.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ParentsDao {
    @Query("SELECT * FROM parents")
    fun getAll(): LiveData<List<Parents>>

    @Update
    fun update(vararg e: Parents?): Int

    @Delete
    fun delete(vararg e: Parents?): Int

    @Insert
    fun insert(e: Parents): Long
}