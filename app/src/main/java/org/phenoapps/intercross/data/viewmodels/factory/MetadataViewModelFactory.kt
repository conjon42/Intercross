package org.phenoapps.intercross.data.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.phenoapps.intercross.data.MetadataRepository
import org.phenoapps.intercross.data.viewmodels.MetadataViewModel

class MetadataViewModelFactory(
        private val repo: MetadataRepository
) : ViewModelProvider.Factory {

    @SuppressWarnings("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        return MetadataViewModel(repo) as T
    }
}