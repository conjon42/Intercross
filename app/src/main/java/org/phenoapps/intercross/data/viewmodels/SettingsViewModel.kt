package org.phenoapps.intercross.data.viewmodels

import androidx.lifecycle.liveData
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.data.models.Settings

class SettingsViewModel internal constructor(repo: SettingsRepository) : BaseViewModel<Settings>(repo) {

    val settings = repo.getSettings()

}