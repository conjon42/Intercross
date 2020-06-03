package org.phenoapps.intercross.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.phenoapps.intercross.data.PollenGroup
import org.phenoapps.intercross.data.PollenGroupRepository

class PollenGroupViewModel internal constructor(
        private val repo: PollenGroupRepository
) : ViewModel() {

    private val viewModelJob = Job()

    //coroutines do not complete until all launched children complete
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val groups: LiveData<List<PollenGroup>> = repo.getAll()

    fun addPollenSet(name: String, id: String) {
        viewModelScope.launch {
            repo.createPollenSet(name, id)
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