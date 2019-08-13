package org.phenoapps.intercross.viewmodels

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.fragments.SummaryFragment

class SummaryViewModel(s: SummaryFragment.SummaryData) : ViewModel() {
    val event = ObservableField<Events>(s.event)
    val female = ObservableField<String>(s.event.femaleObsUnitDbId)
    val male = ObservableField<String>(s.event.maleOBsUnitDbId)
    val count = ObservableField<String>(s.count.toString())
}