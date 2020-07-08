package org.phenoapps.intercross.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import org.phenoapps.intercross.data.models.Event

@Dao
interface EventsDao : BaseDao<Event> {

    data class ParentCount(val mom: String, val dad: String, val count: Int)

    data class ParentData(val momCode: String, val momReadableName: String,
                          val dadCode: String, val dadReadableName: String)

    @Query("SELECT * FROM events WHERE events.eid == :eid")
    suspend fun getEvent(eid: Long?): Event

    @Query("SELECT * FROM events ORDER BY date DESC")
    fun selectAll(): LiveData<List<Event>>

    @Query("""
        SELECT DISTINCT x.mom, x.dad,
            (SELECT COUNT(*)
            FROM events as y
            WHERE y.mom = x.mom and y.dad = x.dad) as count
        FROM events as x
    """)
    fun getParentCount(): LiveData<List<ParentCount>>

    @Query("""
        SELECT m.codeId as momCode, m.name as momReadableName, d.codeId as dadCode, d.name as dadReadableName
        FROM parents as m, parents as d, events as e
        WHERE e.eid = :eid and e.mom = m.codeId and e.dad = d.codeId
    """)
    fun getParents(eid: Long): LiveData<ParentData>

    @Query("""SELECT m.*, d.*
                    FROM events as m, events as d, events as x
                    WHERE d.codeId = x.dad and m.codeId = x.mom AND x.eid = :eid""")
    suspend fun selectEventParents(eid: Long): List<Event>

    @Query("SELECT * FROM events WHERE eid = :eid LIMIT 1")
    suspend fun selectById(eid: Long): Event

    @Query("DELETE FROM events WHERE events.eid = :eid")
    suspend fun deleteById(eid: Long)

    @Query("SELECT DISTINCT x.codeId FROM events as x WHERE x.codeId = :code")
    fun getEventsWithCode(code: String): List<String>

    @Query("SELECT DISTINCT * FROM events WHERE :dbId=codeId")
    fun findCross(dbId: String): LiveData<Event>

    @Query("SELECT * FROM events")
    fun selectAllLive(): LiveData<List<Event>>

    @Query("DELETE FROM events WHERE :dbId = codeId")
    fun delete(dbId: String): Int

    @Query("DELETE FROM events")
    fun deleteAll(): Int

    @Query("SELECT DISTINCT * FROM events")
    fun getCrosses(): List<Event>

    @Query("SELECT * FROM events as e WHERE e.codeId = :name LIMIT 1")
    fun getPollination(name: String): LiveData<Event>

    @Query("SELECT * FROM events as e WHERE e.codeId = :name LIMIT 1")
    fun getHarvest(name: String): LiveData<Event>

    @Query("SELECT * FROM events as e WHERE e.codeId = :name LIMIT 1")
    fun getThresh(name: String): LiveData<Event>

    @Transaction
    @Query("DELETE FROM events")
    fun drop()

    @Insert
    suspend fun insertEvent(event: Event): Long
}