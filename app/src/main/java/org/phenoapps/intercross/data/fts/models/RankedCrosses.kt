package org.phenoapps.intercross.data.fts.models

import androidx.room.Embedded
import org.phenoapps.intercross.data.fts.tables.Crosses

/**
 * Return object of the search query. Match info contains FTS data for matching indices to query terms.
 */
data class RankedCrosses(
    @Embedded
    val cross: Crosses,
    val matchInfo: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RankedCrosses

        if (cross != other.cross) return false
        if (!matchInfo.contentEquals(other.matchInfo)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cross.hashCode()
        result = 31 * result + matchInfo.contentHashCode()
        return result
    }
}