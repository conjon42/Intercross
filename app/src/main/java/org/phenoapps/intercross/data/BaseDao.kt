package org.phenoapps.intercross.data

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {


    @Update
    fun update(vararg e: T?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(e: T): Long

    @Delete
    fun delete(vararg e: T?): Int
}