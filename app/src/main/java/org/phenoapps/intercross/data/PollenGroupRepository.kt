package org.phenoapps.intercross.data

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.phenoapps.intercross.data.dao.PollenGroupDao
import org.phenoapps.intercross.data.models.PollenGroup


class PollenGroupRepository private constructor(
        private val pollenGroupDao: PollenGroupDao
): BaseRepository<PollenGroup>(pollenGroupDao) {

    fun selectAll() = pollenGroupDao.selectAll()

    suspend fun updateSelection(selection: Int) {

        pollenGroupDao.updateSelection(selection)
    }

    suspend fun updateSelectByCode(codeId: String, selected: Boolean) {

        pollenGroupDao.updateSelectByCode(codeId, selected)
    }

    suspend fun deleteByCode(codeId: List<String>) {

        for (code: String in codeId) {

            pollenGroupDao.deleteByCode(code)

        }
    }

    companion object {

        @Volatile private var instance: PollenGroupRepository? = null

        fun getInstance(groupDao: PollenGroupDao) =
                instance ?: synchronized(this) {
                    instance ?: PollenGroupRepository(pollenGroupDao = groupDao)
                            .also { instance = it }
                }
    }

}