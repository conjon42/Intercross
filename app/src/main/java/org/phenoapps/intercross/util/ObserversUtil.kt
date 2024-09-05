package org.phenoapps.intercross.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

//extension function for live data to only observe once when the data is not null
fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: Observer<T>) {
    observe(owner, object : Observer<T> {
        override fun onChanged(value: T) {
            value?.let { data ->
                observer.onChanged(data)
                removeObserver(this)
            }
        }
    })
}
