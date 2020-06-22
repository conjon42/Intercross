package org.phenoapps.intercross.data

import org.phenoapps.intercross.data.dao.PollenGroupDao
import org.phenoapps.intercross.data.models.PollenGroup


class PollenGroupRepository private constructor(
        private val pollenGroupDao: PollenGroupDao
): BaseRepository<PollenGroup>(pollenGroupDao) {

    fun selectAll() = pollenGroupDao.selectAll()




    companion object {
        @Volatile private var instance: PollenGroupRepository? = null

        fun getInstance(groupDao: PollenGroupDao) =
                instance ?: synchronized(this) {
                    instance ?: PollenGroupRepository(pollenGroupDao = groupDao)
                            .also { instance = it }
                }
    }
}