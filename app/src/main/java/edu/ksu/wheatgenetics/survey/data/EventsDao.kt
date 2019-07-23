package edu.ksu.wheatgenetics.survey.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EventsDao {
    @Query("SELECT * FROM events")
    fun getAll(): LiveData<List<Events>>

    @Update
    fun update(vararg e: Events?): Int

    @Insert
    fun insert(e: Events): Long
}