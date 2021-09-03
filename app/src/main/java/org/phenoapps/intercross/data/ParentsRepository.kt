package org.phenoapps.intercross.data

import org.phenoapps.intercross.data.dao.ParentsDao
import org.phenoapps.intercross.data.models.Parent

class ParentsRepository private constructor(
        private val parentsDao: ParentsDao): BaseRepository<Parent>(parentsDao) {

    fun selectAll() = parentsDao.selectAll()

    fun selectAll(sex: Int) = parentsDao.selectAll(sex)

    suspend fun deselectAll() = parentsDao.deselectAll()

    fun insert(parent: Parent): Long = parentsDao.insert(parent)

    suspend fun drop() = parentsDao.drop()

    suspend fun updateSelection(selection: Int) = parentsDao.updateSelection(selection)

    suspend fun insertIgnore(vararg parents: Parent) = parentsDao.insertIgnore(*parents)

    /*
    TODO replace foreach with batch call
     */
    suspend fun updateName(vararg parents: Parent) {

        parents.forEach {

            parentsDao.updateName(it.codeId, it.name)

        }
    }

    companion object {
        @Volatile private var instance: ParentsRepository? = null

        fun getInstance(parentsDao: ParentsDao) =
                instance ?: synchronized(this) {
                    instance ?: ParentsRepository(parentsDao)
                        .also { instance = it }
                }
    }
}