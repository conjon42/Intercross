package org.phenoapps.intercross.data

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class EventsRepository private constructor(private val eventsDao: EventsDao) {

    suspend fun createCrossEvent(e: Events) {
        withContext(IO) {
            eventsDao.insert(e.apply { eventName = EventName.POLLINATION.itemType })
            eventsDao.insert(e.apply { eventName = EventName.HARVEST.itemType })
            eventsDao.insert(e.apply { eventName = EventName.THRESH.itemType })
        }
    }

    suspend fun insert(e: Events) {
        withContext(IO) {
            eventsDao.insert(e)
        }
    }

    suspend fun updateFlowers(e: Events, x: Int) {
        withContext(IO) {
            eventsDao.updateFlower(e.eventDbId, x, e.femaleObsUnitDbId, e.maleOBsUnitDbId)
        }
    }

    suspend fun updateFruit(e: Events, x: Int) {
        withContext(IO) {
            eventsDao.updateFruit(e.eventDbId, x, e.femaleObsUnitDbId, e.maleOBsUnitDbId)
        }
    }

    suspend fun updateSeed(e: Events, x: Int) {
        withContext(IO) {
            eventsDao.updateSeed(e.eventDbId, x, e.femaleObsUnitDbId, e.maleOBsUnitDbId)
        }
    }

    suspend fun update(vararg e: Events?) {
        withContext(IO) {
            eventsDao.update(*e)
        }
    }

    suspend fun delete(e: Events) {
        withContext(IO) {
            eventsDao.delete(e.eventDbId)
        }
    }

    suspend fun deleteAll() {
        withContext(IO) {
            eventsDao.deleteAll()
        }
    }

    fun getAll() = eventsDao.getAll()

    fun getCrosses() = eventsDao.getCrosses()

    fun getThresh(e: Events) = eventsDao.getThresh(e.eventDbId)

    fun getHarvest(e: Events) = eventsDao.getHarvest(e.eventDbId)

    fun getPollination(e: Events) = eventsDao.getPollination(e.eventDbId)

    companion object {
        @Volatile private var instance: EventsRepository? = null

        fun getInstance(eventsDao: EventsDao) =
                instance ?: synchronized(this) {
                    instance ?: EventsRepository(eventsDao)
                        .also { instance = it }
                }
    }
}