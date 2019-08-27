package org.phenoapps.intercross.data

import androidx.room.*

interface BaseDao<T> {


    @Update
    fun update(vararg e: T?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(e: T): Long

    @Delete
    fun delete(vararg e: T?): Int
}