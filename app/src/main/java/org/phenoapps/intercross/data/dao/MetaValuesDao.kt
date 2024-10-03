package org.phenoapps.intercross.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.phenoapps.intercross.data.models.MetadataValues

@Dao
interface MetaValuesDao: BaseDao<MetadataValues> {

    @Query("SELECT * FROM metaValues")
    fun selectAll(): LiveData<List<MetadataValues>>

}