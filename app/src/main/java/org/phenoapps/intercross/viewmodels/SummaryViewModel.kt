package org.phenoapps.intercross.viewmodels

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.fragments.SummaryFragment

class SummaryViewModel(s: SummaryFragment.SummaryData) : ViewModel() {
    val name = ObservableField<String>(s.name)
    val count = ObservableField<String>(s.count.toString())
}