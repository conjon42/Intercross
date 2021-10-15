package org.phenoapps.intercross.data.fts.dao

import androidx.room.*
import org.phenoapps.intercross.data.fts.tables.Crosses

@Dao
interface CrossesDao {

    @Update
    fun update(vararg e: Crosses?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg items: Crosses)

    @Delete
    fun delete(vararg e: Crosses): Int

}