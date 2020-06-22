package org.phenoapps.intercross.data.models.relations

import androidx.room.Embedded
import org.phenoapps.intercross.data.models.Parent

data class MomDad(
        @Embedded
        val mom: Parent?,
        @Embedded(prefix = "d")
        val dad: Parent?
)
