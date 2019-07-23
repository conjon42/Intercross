package edu.ksu.wheatgenetics.survey.viewmodels

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import edu.ksu.wheatgenetics.survey.data.Events
import edu.ksu.wheatgenetics.survey.data.Experiment

class EventsViewModel(event: Events) : ViewModel() {
    val name = ObservableField<String>(event.eventDbId)
    val date = ObservableField<String>(event.date)
    val male = ObservableField<String>(event.maleOBsUnitDbId)
    val female = ObservableField<String>(event.femaleObsUnitDbId)
    val count = ObservableField<Int>(event.eid)
}