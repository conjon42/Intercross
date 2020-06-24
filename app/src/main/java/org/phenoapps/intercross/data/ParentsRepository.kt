package org.phenoapps.intercross.data

import org.phenoapps.intercross.data.dao.ParentsDao
import org.phenoapps.intercross.data.models.Parent

class ParentsRepository private constructor(
        private val parentsDao: ParentsDao): BaseRepository<Parent>(parentsDao) {

    fun selectAll() = parentsDao.selectAll()

    fun selectAll(sex: Int) = parentsDao.selectAll(sex)

    suspend fun updateSelection(selection: Int) = parentsDao.updateSelection(selection)

    companion object {
        @Volatile private var instance: ParentsRepository? = null

        fun getInstance(parentsDao: ParentsDao) =
                instance ?: synchronized(this) {
                    instance ?: ParentsRepository(parentsDao)
                        .also { instance = it }
                }
    }
}