package org.phenoapps.intercross.data.viewmodels

import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.models.Event

/**
 * This view model handles loading the events from the DB asynchronously,
 *  and transforms the model into event detail view models for serving to the UI.
 *
 *  This list is potentially null, and should be observed during onCreate.
 */
class EventListViewModel(private val eventRepo: EventsRepository): BaseViewModel<Event>(eventRepo) {

    fun deleteById(eid: Long) {

        eventRepo.deleteById(eid)

    }

    val parents = eventRepo.getParentCount()

    val events = eventRepo.selectAll()

}
