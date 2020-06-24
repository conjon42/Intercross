package org.phenoapps.intercross.data.viewmodels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.models.Parent

class ParentsListViewModel(private val repo: ParentsRepository): BaseViewModel<Parent>(repo) {

    val parents = repo.selectAll()

    val males = repo.selectAll(1)

    val females = repo.selectAll(0)

    fun updateSelection(selection: Int) {

        viewModelScope.launch {

            repo.updateSelection(selection)

        }
    }
}
