package org.phenoapps.intercross.data

import org.phenoapps.intercross.data.dao.MetaValuesDao
import org.phenoapps.intercross.data.models.MetadataValues

class MetaValuesRepository
    private constructor(private val dao: MetaValuesDao): BaseRepository<MetadataValues>(dao) {

    fun selectAll() = dao.selectAll()

    companion object {
        @Volatile private var instance: MetaValuesRepository? = null

        fun getInstance(dao: MetaValuesDao) =
                instance ?: synchronized(this) {
                    instance ?: MetaValuesRepository(dao)
                        .also { instance = it }
                }
    }
}