package org.phenoapps.intercross.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.phenoapps.intercross.data.*

class PollenGroupViewModel internal constructor(
        private val repo: PollenGroupRepository
) : ViewModel() {

    private val viewModelJob = Job()

    //coroutines do not complete until all launched children complete
    private val viewModelScope = CoroutineScope(Main + viewModelJob)

    val groups: LiveData<List<PollenGroup>> = repo.getAll()

    fun addPollenSet(name: String) {
        viewModelScope.launch {
            repo.createPollenSet(name)
        }
    }

    fun delete(vararg p: PollenGroup) {
        viewModelScope.launch {
            repo.delete(*p)
        }
    }
    fun update(vararg p: PollenGroup) {
        viewModelScope.launch {
            repo.update(*p)
        }
    }
}

/*class PollenViewModel internal constructor(
        private val repo: PollenGroupRepository
) : ViewModel() {

    /*private val viewModelJob = Job()

    //coroutines do not complete until all launched children complete
    private val viewModelScope = CoroutineScope(Main + viewModelJob)

    //val pollen: LiveData<List<Pollen>> = repo.getAll()

    fun addPollenSet(name: String) {
        viewModelScope.launch {
            repo.createPollenSet(name)
        }
    }

    fun delete(vararg p: Pollen) {
        viewModelScope.launch {
            repo.delete(*p)
        }
    }
    fun update(vararg p: Pollen) {
        viewModelScope.launch {
            repo.update(*p)
        }
    }
}**/
*/
