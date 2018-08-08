package org.phenoapps.intercross

class AdapterEntry {

    var first: String = String()

    var second: String = String()

    var id: Int = -1

    constructor()

    constructor(first: String, second: String) {
        this.first = first
        this.second = second
    }

    constructor(first: String, second: String, id: Int) {
        this.first = first
        this.second = second
        this.id = id
    }

    fun clear() {
        first = String()
        second = String()
        id = -1
    }
}