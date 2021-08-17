package org.phenoapps.intercross.data.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.phenoapps.intercross.data.MetaValuesRepository
import org.phenoapps.intercross.data.viewmodels.MetaValuesViewModel

class MetaValuesViewModelFactory(
        private val repo: MetaValuesRepository
) : ViewModelProvider.Factory {

    @SuppressWarnings("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        return MetaValuesViewModel(repo) as T
    }
}