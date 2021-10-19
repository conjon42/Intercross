package org.phenoapps.intercross.data

import org.phenoapps.intercross.data.dao.MetadataDao
import org.phenoapps.intercross.data.models.Metadata

class MetadataRepository
    private constructor(private val dao: MetadataDao): BaseRepository<Metadata>(dao) {

    fun selectAll() = dao.selectAll()

    fun getId(property: String) = dao.getId(property)

    fun insert(data: Metadata): Long = dao.insert(data)

    companion object {
        @Volatile private var instance: MetadataRepository? = null

        fun getInstance(dao: MetadataDao) =
                instance ?: synchronized(this) {
                    instance ?: MetadataRepository(dao)
                        .also { instance = it }
                }
    }
}