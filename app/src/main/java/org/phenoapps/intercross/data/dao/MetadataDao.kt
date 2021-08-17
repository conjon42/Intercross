package org.phenoapps.intercross.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import org.phenoapps.intercross.data.models.Settings
import org.phenoapps.intercross.data.models.Metadata

@Dao
interface MetadataDao: BaseDao<Metadata> {

    @Query("SELECT * FROM metadata")
    fun selectAll(): LiveData<List<Metadata>>

    @Query("SELECT DISTINCT property FROM metadata")
    fun getMetaProperties(): LiveData<List<String>>

}