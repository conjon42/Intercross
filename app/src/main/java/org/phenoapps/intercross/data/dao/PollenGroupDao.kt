package org.phenoapps.intercross.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import org.phenoapps.intercross.data.models.PollenGroup

@Dao
interface PollenGroupDao: BaseDao<PollenGroup> {

    @Query("SELECT * FROM pollen_groups")
    fun selectAll(): LiveData<List<PollenGroup>>
}