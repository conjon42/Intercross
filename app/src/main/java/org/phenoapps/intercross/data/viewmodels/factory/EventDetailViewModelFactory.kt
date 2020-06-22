package org.phenoapps.intercross.data.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.viewmodels.EventDetailViewModel

class EventDetailViewModelFactory(private val repo: EventsRepository,
                                  private val eventId: Long
) : ViewModelProvider.Factory {

    @SuppressWarnings("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        return EventDetailViewModel(repo, eventId) as T
    }
}