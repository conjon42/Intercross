package org.phenoapps.intercross.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import org.phenoapps.intercross.data.models.PollenGroup

@Dao
interface PollenGroupDao: BaseDao<PollenGroup> {

    @Query("SELECT * FROM pollen_groups")
    fun selectAll(): LiveData<List<PollenGroup>>

    @Query("DELETE FROM pollen_groups WHERE codeId = :codeId")
    suspend fun deleteByCode(codeId: String)

    @Query("UPDATE pollen_groups SET selected = :selected WHERE codeId = :codeId")
    suspend fun updateSelectByCode(codeId: String, selected: Boolean)
}