package org.phenoapps.intercross.data

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class ParentsRepository private constructor(
        private val parentsDao: ParentsDao
) {
    suspend fun createParents(parentDbId: String, parentName: String,
                            parentType: String, order: String) {
        withContext(IO) {
            parentsDao.insert(Parents(parentDbId, parentName, parentType, order))
        }
    }

    suspend fun update(vararg p: Parents?) {
        withContext(IO) {
            parentsDao.update(*p)
        }
    }

    suspend fun delete(vararg p: Parents?) {
        withContext(IO) {
            parentsDao.delete(*p)
        }
    }

    fun getAll() = parentsDao.getAll()

    companion object {
        @Volatile private var instance: ParentsRepository? = null

        fun getInstance(parentsDao: ParentsDao) =
                instance ?: synchronized(this) {
                    instance ?: ParentsRepository(parentsDao)
                        .also { instance = it }
                }
    }
}