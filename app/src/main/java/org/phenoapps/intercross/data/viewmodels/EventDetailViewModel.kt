package org.phenoapps.intercross.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import org.phenoapps.intercross.data.EventsRepository

class EventDetailViewModel(
        private val eventsRepository: EventsRepository,
        private val eventId: Long
): ViewModel() {

    val event = liveData {

        val data = eventsRepository.getEvent(eventId)

        emit(data)
    }

    val metadata = eventsRepository.getMetadata(eventId)

    val parents = eventsRepository.getParents(eventId)

}