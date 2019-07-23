package edu.ksu.wheatgenetics.survey.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import edu.ksu.wheatgenetics.survey.data.Events
import edu.ksu.wheatgenetics.survey.data.EventsRepository
import edu.ksu.wheatgenetics.survey.data.Experiment
import edu.ksu.wheatgenetics.survey.data.ExperimentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
}