package org.phenoapps.intercross.data.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.phenoapps.intercross.data.PollenGroupRepository
import org.phenoapps.intercross.data.viewmodels.PollenGroupListViewModel

class PollenGroupListViewModelFactory(val repo: PollenGroupRepository): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return PollenGroupListViewModel(repo) as T
    }
}