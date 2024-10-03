package org.phenoapps.intercross.data.viewmodels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.phenoapps.intercross.data.PollenGroupRepository
import org.phenoapps.intercross.data.models.PollenGroup

class PollenGroupListViewModel(private val repo: PollenGroupRepository): BaseViewModel<PollenGroup>(repo) {

    val groups = repo.selectAll()

    fun updateSelection(selection: Int) {

        viewModelScope.launch {

            repo.updateSelection(selection)

        }
    }

    fun updateSelectByCode(codeId: String, selected: Boolean) {

        viewModelScope.launch {

            repo.updateSelectByCode(codeId, selected)

        }
    }

    fun updateSelectByCodes(ids: Array<String>, selected: Boolean) {

        viewModelScope.launch {

            repo.updateSelectByCodes(ids, selected)
        }
    }

    fun deleteByCode(codeId: List<String>) {

        viewModelScope.launch {

            repo.deleteByCode(codeId)

        }
    }
}
