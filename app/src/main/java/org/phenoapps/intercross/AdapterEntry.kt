package org.phenoapps.intercross

import android.widget.Adapter

class AdapterEntry {

    var first: String = String()

    var second: String = String()

    constructor()

    constructor(first: String, second: String) {
        this.first = first
        this.second = second
    }

    fun clear() {
        first = String()
        second = String()
    }
}