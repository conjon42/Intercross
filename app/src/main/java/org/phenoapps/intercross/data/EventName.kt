package org.phenoapps.intercross.data

enum class EventName {
    POLLINATION {
        override val itemType = "flower"
    }, HARVEST {
        override val itemType = "fruit"
    }, THRESH {
        override val itemType = "seed"
    };
    abstract val itemType: String
}