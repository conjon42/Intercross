package org.phenoapps.intercross.data.models

import androidx.room.DatabaseView
import androidx.room.Embedded

/***
 * This class represents a defined view for the Intercross schema.
 * This view helps organize and output events-to-parents pairs.
 *
 * Uses left join to return all events that are paired with parents. If the
 * event does not have parents, the event is still returned.
 */
@DatabaseView("""
SELECT x.*, m.*, d.*, em.*, ed.*
FROM events as x
LEFT JOIN parents as m ON x.mom = m.codeId
LEFT JOIN parents as d ON x.dad = d.codeId
LEFT OUTER JOIN events as em ON em.codeId = x.mom
LEFT OUTER JOIN events as ed ON ed.codeId = x.dad
""")
data class EventParentsView(
        @Embedded
        val event: Event,
        @Embedded(prefix="mom_")
        val eventMom: Parent?,
        @Embedded(prefix="dad_")
        val eventDad: Parent?,
        @Embedded(prefix="dadEvent_")
        val dadFromEvent: Event?,
        @Embedded(prefix = "momEvent_")
        val momFromEvent: Event?
)