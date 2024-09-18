package org.phenoapps.intercross.data.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T> {

    @Update
    fun update(vararg e: T): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg items: T)

    @Delete
    fun delete(vararg e: T): Int
}