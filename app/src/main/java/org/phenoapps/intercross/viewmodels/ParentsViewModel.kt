package org.phenoapps.intercross.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.phenoapps.intercross.data.Parents
import org.phenoapps.intercross.data.ParentsRepository

class ParentsViewModel internal constructor(
        private val repo: ParentsRepository
) : ViewModel() {

    private val viewModelJob = Job()

    //coroutines do not complete until all launched children complete
    private val viewModelScope = CoroutineScope(Main + viewModelJob)

    val parents: LiveData<List<Parents>> = repo.getAll()

    /** eventDbId is the generated or user-entered cross ID **/
    fun addParents(parentsDbId: String, parentsName: String, parentsType: String, order: String) {
        viewModelScope.launch {
            repo.createParents(parentsDbId, parentsName, parentsType, order)
        }
    }

    fun delete(vararg p: Parents) {
        viewModelScope.launch {
            repo.delete(*p)
        }
    }

    fun update(vararg p: Parents) {
        viewModelScope.launch {
            repo.update(*p)
        }
    }
}