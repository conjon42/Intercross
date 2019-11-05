package org.phenoapps.intercross.data

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext


class PollenRepository private constructor(
        private val pollenDao: PollenDao
) {


    suspend fun createPollen(groupId: Long, name: String) {
        withContext(IO) {
            pollenDao.insert(Pollen(null, groupId, name))
        }
    }

    suspend fun update(vararg p: Pollen?) {
        withContext(IO) {
            pollenDao.update(*p)
        }
    }

    suspend fun delete(vararg p: Pollen?) {
        withContext(IO) {
            pollenDao.delete(*p)
        }
    }

    fun getAll() = pollenDao.getAll()

    companion object {
        @Volatile private var instance: PollenRepository? = null

        fun getInstance(pollenDao: PollenDao) =
                instance ?: synchronized(this) {
                    instance ?: PollenRepository(pollenDao = pollenDao)
                            .also { instance = it }
                }
    }
}
