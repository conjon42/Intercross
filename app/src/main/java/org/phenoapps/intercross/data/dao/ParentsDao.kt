package org.phenoapps.intercross.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.phenoapps.intercross.data.models.Parent

@Dao
interface ParentsDao : BaseDao<Parent> {

    @Query("SELECT * FROM parents")
    fun selectAll(): LiveData<List<Parent>>

    @Query("SELECT * FROM parents WHERE parents.sex == :sex")
    fun selectAll(sex: Int): LiveData<List<Parent>>

    @Query("UPDATE parents SET selected = :selection")
    suspend fun updateSelection(selection: Int)

}