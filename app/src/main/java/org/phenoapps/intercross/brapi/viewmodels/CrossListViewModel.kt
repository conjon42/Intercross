package org.phenoapps.intercross.brapi.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import org.phenoapps.intercross.brapi.repository.CrossRepository

class CrossListViewModel(private val token: String): ViewModel() {

    val repo = CrossRepository(this.token)

    val crosses = liveData(Dispatchers.IO) {

        val data = repo.getCrosses()

        emit(data)

    }

}