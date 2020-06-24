package org.phenoapps.intercross.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import org.phenoapps.intercross.data.models.Parent

@Dao
interface ParentsDao : BaseDao<Parent> {

    @Query("SELECT * FROM parents")
    fun selectAll(): LiveData<List<Parent>>

    @Query("SELECT * FROM parents WHERE parents.sex == :sex")
    fun selectAll(sex: Int): LiveData<List<Parent>>

    @Query("UPDATE parents SET selected = :selection")
    suspend fun updateSelection(selection: Int)

    @Query("UPDATE parents SET name = :name WHERE codeId = :code")
    suspend fun updateName(code: String, name: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(vararg parents: Parent)

    @Query("DELETE FROM parents")
    suspend fun drop()
}