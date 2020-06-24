package org.phenoapps.intercross.data.dao

import androidx.room.*

interface BaseDao<T> {

    @Update
    fun update(vararg e: T?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg items: T)

    @Delete
    fun delete(vararg e: T?): Int

}