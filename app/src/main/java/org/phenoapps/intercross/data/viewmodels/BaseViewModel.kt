package org.phenoapps.intercross.data.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.phenoapps.intercross.data.BaseRepository

open class BaseViewModel<T> internal constructor(private val repo: BaseRepository<T>) : ViewModel() {

    private val viewModelJob = Job()

    //coroutines do not complete until all launched children complete
    private val viewModelScope = CoroutineScope(Main + viewModelJob)

    fun delete(vararg items: T) {

        viewModelScope.launch {

            repo.delete(*items)
        }
    }

    fun update(vararg items: T) {

        viewModelScope.launch {

            repo.update(*items)
        }
    }

    fun insert(vararg items: T) {

        viewModelScope.launch {

            repo.insert(*items)
        }
    }
}