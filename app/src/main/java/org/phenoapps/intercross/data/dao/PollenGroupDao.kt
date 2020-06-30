package org.phenoapps.intercross.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.phenoapps.intercross.data.models.PollenGroup

@Dao
interface PollenGroupDao: BaseDao<PollenGroup> {

    @Query("SELECT DISTINCT * FROM pollen_groups")
    fun selectAll(): LiveData<List<PollenGroup>>

    @Query("DELETE FROM pollen_groups WHERE codeId = :codeId")
    suspend fun deleteByCode(codeId: String)

    @Query("UPDATE pollen_groups SET selected = :selected WHERE codeId = :codeId")
    suspend fun updateSelectByCode(codeId: String, selected: Boolean)

    @Query("UPDATE pollen_groups SET selected = :selection")
    suspend fun updateSelection(selection: Int)
}