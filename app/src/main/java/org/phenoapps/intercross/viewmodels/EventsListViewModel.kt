package org.phenoapps.intercross.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.data.EventsRepository

class EventsListViewModel internal constructor(
        private val repo: EventsRepository
) : ViewModel() {

    private val viewModelJob = Job()

    //coroutines do not complete until all launched children complete
    private val viewModelScope = CoroutineScope(Main + viewModelJob)

    val events: LiveData<List<Events>> = repo.getAll()

    /** eventDbId is the generated or user-entered cross ID **/
    fun addCrossEvent(eventDbId: String, femaleId: String, maleId: String) {

        viewModelScope.launch {
            repo.createEvent(0, eventDbId, 0, femaleId, maleId)
        }
    }

    fun delete(vararg e: Events) {
        viewModelScope.launch {
            repo.delete(*e)
        }
    }
    fun update(vararg e: Events) {
        viewModelScope.launch {
            repo.update(*e)
        }
    }
}