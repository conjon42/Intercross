package edu.ksu.wheatgenetics.survey.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SampleRepository private constructor(
        private val sampleDao: SampleDao
) {
    suspend fun createSample(eid: Int, sampleName: String, lat: Double,
                             lng: Double, person: String, plot: String) {
        withContext(Dispatchers.IO) {
            sampleDao.insert(Sample(eid, sampleName, lat, lng, person, plot))
        }
    }

    suspend fun updateSamples(vararg s: Sample) {
        withContext(Dispatchers.IO) {
            sampleDao.updateSamples(*s)
        }
    }

    fun getPlot(eid: Int, plot: String) = sampleDao.getPlot(eid, plot)

    fun getPlotNames(eid: Int) = sampleDao.getPlotNames(eid)

    fun getAll(eid: Int) = sampleDao.getAll(eid)

    companion object {
        @Volatile private var instance: SampleRepository? = null

        fun getInstance(sampleDao: SampleDao) =
                instance ?: synchronized(this) {
                    instance ?: SampleRepository(sampleDao)
                            .also { instance = it }
                }
    }
}