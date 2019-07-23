package edu.ksu.wheatgenetics.survey.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EventIdsDao {

    @Update
    fun update(vararg e: EventIds): Int

    @Insert
    fun insert(e: EventIds): Long
}