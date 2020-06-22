package org.phenoapps.intercross.data

import org.phenoapps.intercross.data.dao.SettingsDao
import org.phenoapps.intercross.data.models.Settings

class SettingsRepository private constructor(
        private val settingsDao: SettingsDao): BaseRepository<Settings>(settingsDao) {

    suspend fun getSettings() = settingsDao.getSettings()

    companion object {
        @Volatile private var instance: SettingsRepository? = null

        fun getInstance(settingsDao: SettingsDao) =
                instance ?: synchronized(this) {
                    instance ?: SettingsRepository(settingsDao)
                            .also { instance = it }
                }
    }

}