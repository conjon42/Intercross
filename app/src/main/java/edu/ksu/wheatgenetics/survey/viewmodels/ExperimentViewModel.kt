package edu.ksu.wheatgenetics.survey.viewmodels

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import edu.ksu.wheatgenetics.survey.data.Experiment

class ExperimentViewModel(experiment: Experiment) : ViewModel() {
    val name = ObservableField<String>(experiment.name)
    val count = ObservableField<Int>(experiment.count)
}