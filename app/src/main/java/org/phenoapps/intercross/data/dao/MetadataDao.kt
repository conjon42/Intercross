package org.phenoapps.intercross.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import org.phenoapps.intercross.data.models.Meta

@Dao
interface MetadataDao: BaseDao<Meta> {

    @Query("SELECT * FROM metadata")
    fun selectAll(): LiveData<List<Meta>>

    @Query("SELECT mid FROM metadata WHERE metadata.property = :property")
    fun getId(property: String): Int

    @Query("SELECT DISTINCT property FROM metadata")
    fun getMetaProperties(): LiveData<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(data: Meta): Long
}