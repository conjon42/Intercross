package org.phenoapps.intercross.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = :id LIMIT 1")
    fun getSettings(id: Int): LiveData<Settings>

    @Update
    fun update(s: Settings): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(s: Settings): Long

    @Delete
    fun delete(vararg s: Settings?): Int
}