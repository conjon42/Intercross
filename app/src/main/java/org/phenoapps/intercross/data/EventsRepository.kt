package org.phenoapps.intercross.data

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class EventsRepository private constructor(
        private val eventsDao: EventsDao
) {
    suspend fun createEvent(eventId: Int, eventDbId: String, eventValue: Int,
                            femaleId: String, maleId: String) {
        withContext(IO) {
            eventsDao.insert(Events(eventId, eventDbId, eventValue, femaleId, maleId))
        }
    }

    suspend fun update(vararg e: Events?) {
        withContext(IO) {
            eventsDao.update(*e)
        }
    }

    suspend fun delete(vararg e: Events?) {
        withContext(IO) {
            eventsDao.delete(*e)
        }
    }

    fun getAll() = eventsDao.getAll()

    companion object {
        @Volatile private var instance: EventsRepository? = null

        fun getInstance(eventsDao: EventsDao) =
                instance ?: synchronized(this) {
                    instance ?: EventsRepository(eventsDao)
                        .also { instance = it }
                }
    }
}