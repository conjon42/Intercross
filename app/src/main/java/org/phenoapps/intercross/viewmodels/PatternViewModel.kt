package org.phenoapps.intercross.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

//shared view model
class PatternViewModel(uuid: Boolean = false, a: Boolean = false, s: String = "", pre: String = "", n: Int = 0, p: Int = 3) : ViewModel() {

    val uuid: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val auto: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val suffix: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val prefix: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val number: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val pad: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun getPattern(): String =
        "$prefix$number$suffix"

    val pattern: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            this.value = "$prefix$number$suffix"
        }
    }

    init {
        this.uuid.value = uuid
        auto.value = a
        suffix.value = s
        prefix.value = pre
        number.value = n
        pad.value = p
    }
}