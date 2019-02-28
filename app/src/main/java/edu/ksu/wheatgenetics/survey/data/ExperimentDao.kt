package edu.ksu.wheatgenetics.survey.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExperimentDao {
    @Query("SELECT * FROM experiments")
    fun getAll(): LiveData<List<Experiment>>

    @Update
    fun update(vararg e: Experiment?): Int

    @Insert
    fun insert(e: Experiment): Long
}