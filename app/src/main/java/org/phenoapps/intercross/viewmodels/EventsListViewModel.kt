package org.phenoapps.intercross.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.phenoapps.intercross.data.EventName
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.data.EventsRepository

class EventsListViewModel internal constructor(
        private val repo: EventsRepository) : ViewModel() {

    private val viewModelJob = Job()

    //coroutines do not complete until all launched children complete
    private val viewModelScope = CoroutineScope(Main + viewModelJob)

    val events: LiveData<List<Events>> = repo.getAll()

    val crosses: LiveData<List<Events>> = repo.getCrosses()

    fun getHarvest(e: Events): LiveData<Events> = repo.getHarvest(e)
    fun getThresh(e: Events): LiveData<Events> = repo.getThresh(e)
    fun getPollination(e: Events): LiveData<Events> = repo.getPollination(e)


    fun addCrossEvent(event: Events) {

        viewModelScope.launch {
            repo.createCrossEvent(event)
        }
    }

    fun updateFlowers(event: Events, x: Int) {

        viewModelScope.launch {
            repo.updateFlowers(event, x)
        }
    }

    fun updateFruits(event: Events, x: Int) {

        viewModelScope.launch {
            repo.updateFruit(event, x)
        }
    }

    fun updateSeeds(event: Events, x: Int) {

        viewModelScope.launch {
            repo.updateSeed(event, x)
        }
    }

    fun addEvent(event: Events) {

        viewModelScope.launch {
            repo.insert(event)
        }
    }

    fun delete(e: Events) {
        viewModelScope.launch {
            repo.delete(e)
        }
    }
}