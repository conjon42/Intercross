package org.phenoapps.intercross.data.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.models.Event

class EventProducer constructor(
        private val repo: EventsRepository) : BaseViewModel<Event>(repo) {

    val events: LiveData<List<Event>> = repo.loadCrosses()

    ///val parents: LiveData<List<EventsDao.ParentsCount>> = repo.loadParents()

    //fun getParents(e: Event): Array<LiveData<Event>> = repo.getParents(e)

    fun resetSelections() {

        viewModelScope.launch {

            //repo.resetSelections()
        }
    }

//    fun addCrossEvent(event: Event) {
//
//        viewModelScope.launch {
//
//            repo.createCrossEvent(event)
//        }
//    }

    fun deleteAll() {

        viewModelScope.launch {

            repo.deleteAll()
        }
    }
}