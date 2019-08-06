package org.phenoapps.intercross.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.phenoapps.intercross.data.Settings
import org.phenoapps.intercross.data.SettingsRepository

class SettingsViewModel internal constructor(
        private val repo: SettingsRepository) : ViewModel() {

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val settings: LiveData<Settings> = repo.getSettings()

    fun addSetting(s: Settings) {
        viewModelScope.launch {
            repo.createSettings(s)
        }
    }

    fun delete(vararg e: Settings) {
        viewModelScope.launch {
            repo.delete(*e)
        }
    }
    fun update(s: Settings) {

        viewModelScope.launch {
            repo.update(s)
        }
    }
}