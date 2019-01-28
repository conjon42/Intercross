package org.phenoapps.intercross

class AdapterEntry {

    var first: String = String()

    var second: String = String()

    var third: String = String()

    var id: Int = -1

    constructor()

    constructor(first: String, second: String, third: String) {
        this.first = first
        this.second = second
        this.third = third
    }

    constructor(first: String, second: String) {
        this.first = first
        this.second = second
    }

    constructor(first: String){
        this.first = first
    }

    fun clear() {
        first = String()
        second = String()
        third = String()
        id = -1
    }
}