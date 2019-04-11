package org.phenoapps.intercross

class AdapterEntry {

    var first: String = String()

    var second: String = String()

    var third: String = String()

    var completed: Boolean = false

    var id: Int = -1

    constructor()

    constructor(first: String, second: String) {
        this.first = first
        this.second = second
    }

    constructor(first: String) {
        this.first = first
    }

    constructor(s: String, s1: String, s2: String) {
        this.first = s
        this.second = s1
        this.third = s2
    }

}