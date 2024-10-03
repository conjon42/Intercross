package org.phenoapps.intercross.data.fts.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.withContext
import org.phenoapps.intercross.data.dao.EventsDao
import org.phenoapps.intercross.data.fts.dao.CrossesDao
import org.phenoapps.intercross.data.fts.dao.CrossesFtsDao
import org.phenoapps.intercross.data.fts.models.RankedCrosses
import org.phenoapps.intercross.data.fts.tables.Crosses
import org.phenoapps.intercross.data.models.Event

class CrossesViewModel(
    private val crossesDao: CrossesDao,
    private val fts: CrossesFtsDao
): ViewModel() {

    private fun sanitizeQuery(query: String) = query.replace("\"", "\"\"")

    fun searchResult(query: String) = fts.search("*${sanitizeQuery(query)}*")

    //replace all - in timestamps with _ because it is an fts operator
    //inserts crossess into the fts database, links events with their parents (whether commutative or not)
    fun insert(commutative: Boolean, events: List<Event>, parents: List<EventsDao.ParentCount>) {
        crossesDao.insert(*events.map { cross ->
            val parent = if (!commutative) parents.find { it.dad == cross.maleObsUnitDbId && it.mom == cross.femaleObsUnitDbId }
                         else parents.find { (it.dad == cross.maleObsUnitDbId && it.mom == cross.femaleObsUnitDbId)
                                        || (it.mom == cross.maleObsUnitDbId && it.dad == cross.femaleObsUnitDbId)}
            Crosses(cross.id ?: -1L, cross.eventDbId, cross.femaleObsUnitDbId, cross.maleObsUnitDbId,
                parent?.momReadable, parent?.dadReadable, cross.timestamp.replace("-", "_"))
        }.toTypedArray())
    }
}
