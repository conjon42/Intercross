package org.phenoapps.intercross.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CrossSharedViewModel : ViewModel() {
    val male: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val female: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val name: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val lastScan: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}