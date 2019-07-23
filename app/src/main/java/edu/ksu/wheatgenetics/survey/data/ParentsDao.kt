package edu.ksu.wheatgenetics.survey.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ParentsDao {
    @Query("SELECT * FROM parents")
    fun getAll(): LiveData<List<Parents>>

    @Update
    fun update(vararg e: Parents?): Int

    @Insert
    fun insert(e: Parents): Long
}