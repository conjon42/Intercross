package org.phenoapps.intercross.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EventsDao {
    @Query("SELECT * FROM events")
    fun getAll(): LiveData<List<Events>>

    @Update
    fun update(vararg e: Events?): Int

    @Insert
    fun insert(e: Events): Long

    @Delete
    fun delete(vararg e: Events?): Int
}