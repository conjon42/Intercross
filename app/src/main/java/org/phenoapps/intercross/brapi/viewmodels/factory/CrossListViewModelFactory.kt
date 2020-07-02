package org.phenoapps.intercross.brapi.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.phenoapps.intercross.brapi.viewmodels.CrossListViewModel

class CrossListViewModelFactory(private val token: String) : ViewModelProvider.Factory {

    @SuppressWarnings("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        return CrossListViewModel(token) as T
    }
}