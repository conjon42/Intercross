package org.phenoapps.intercross.test

import org.phenoapps.intercross.data.models.Event
import java.util.*
import kotlin.collections.ArrayList

class TestUtil {

    companion object {


        /**
         * Creates a variable number of Events
         */
        fun createEvent(num: Int): List<Event> {

            val output = ArrayList<Event>()

            for (i in 0..num) {

                output.add(Event(UUID.randomUUID().toString()))

            }

            return output
        }
    }
}