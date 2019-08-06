package org.phenoapps.intercross.data

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class SettingsRepository private constructor(
        private val settingsDao: SettingsDao
) {
    suspend fun createSettings(s: Settings) {
        withContext(IO) {
            settingsDao.insert(s.apply { id = 0 })
        }
    }

    suspend fun update(s: Settings) {
        withContext(IO) {
            settingsDao.update(s)
        }
    }

    suspend fun delete(vararg e: Settings?) {
        withContext(IO) {
            settingsDao.delete(*e)
        }
    }

    fun getSettings() = settingsDao.getSettings(0)

    companion object {
        @Volatile private var instance: SettingsRepository? = null

        fun getInstance(settingsDao: SettingsDao) =
                instance ?: synchronized(this) {
                    instance ?: SettingsRepository(settingsDao)
                            .also { instance = it }
                }
    }

}