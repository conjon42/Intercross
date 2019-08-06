package org.phenoapps.intercross.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsSharedViewModel : ViewModel() {
    val order: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val pattern: MutableLiveData<PatternViewModel> by lazy {
        MutableLiveData<PatternViewModel>()
    }
    val autoPattern: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val autoUUID: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val allowBlank: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
}