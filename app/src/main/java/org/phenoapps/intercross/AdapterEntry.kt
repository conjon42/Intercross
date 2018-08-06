package org.phenoapps.intercross

import android.widget.Adapter

class AdapterEntry {

    var first: String = String()

    var second: String = String()

    var id: String = String()

    constructor()

    constructor(first: String, second: String, id: String) {
        this.first = first
        this.second = second
        this.id = id
    }

    fun clear() {
        first = String()
        second = String()
        id = String()
    }
}