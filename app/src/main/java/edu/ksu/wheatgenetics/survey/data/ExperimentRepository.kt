package edu.ksu.wheatgenetics.survey.data

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class ExperimentRepository private constructor(
        private val experimentDao: ExperimentDao
) {
    suspend fun createExperiment(experimentId: String) {
        withContext(IO) {
            experimentDao.insert(Experiment(experimentId))
        }
    }

    suspend fun update(vararg e: Experiment?) {
        withContext(IO) {
            experimentDao.update(*e)
        }
    }

    fun getAll() = experimentDao.getAll()

    companion object {
        @Volatile private var instance: ExperimentRepository? = null

        fun getInstance(experimentDao: ExperimentDao) =
                instance ?: synchronized(this) {
                    instance ?: ExperimentRepository(experimentDao)
                        .also { instance = it }
                }
    }
}