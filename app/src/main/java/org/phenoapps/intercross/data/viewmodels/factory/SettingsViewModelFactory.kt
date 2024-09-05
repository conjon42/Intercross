package org.phenoapps.intercross.data.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel

class SettingsViewModelFactory(private val repo: SettingsRepository
) : ViewModelProvider.Factory {

    @SuppressWarnings("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return SettingsViewModel(repo) as T
    }
}