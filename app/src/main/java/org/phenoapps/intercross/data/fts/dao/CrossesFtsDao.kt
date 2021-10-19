package org.phenoapps.intercross.data.fts.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import org.phenoapps.intercross.data.fts.models.RankedCrosses

@Dao
interface CrossesFtsDao {

    @Query("""
        SELECT crosses.*, matchinfo(crosses_fts, "pcx") as "matchInfo"
        FROM crosses
        JOIN crosses_fts ON crosses_fts.rowid = crosses.rowid
        WHERE crosses_fts MATCH :query
    """)
    fun search(query: String): LiveData<List<RankedCrosses>>

    @Query("""INSERT INTO crosses_fts (crosses_fts) VALUES ("rebuild")""")
    fun rebuild()
}