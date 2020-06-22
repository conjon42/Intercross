package org.phenoapps.intercross.data.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.viewmodels.EventListViewModel

class EventsListViewModelFactory(
        private val repo: EventsRepository
) : ViewModelProvider.Factory {

    @SuppressWarnings("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        return EventListViewModel(repo) as T
    }
}