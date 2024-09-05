package org.phenoapps.intercross.data.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel

/**
 * Model View List factory for serving rows from the Parents table.
 * Mainly used for printing individual barcodes that are not considered events.
 */
class ParentsListViewModelFactory(val repo: ParentsRepository): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return ParentsListViewModel(repo) as T
    }
}