package org.phenoapps.intercross.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EventsDao : BaseDao<Events> {

    @Query("UPDATE events SET eventValue = :x WHERE eventName = 'flower' AND eventDbId = :name AND femaleObsUnitDbId = :female AND maleOBsUnitDbId = :male")
    fun updateFlower(name: String, x: Int, female: String, male: String)

    @Query("UPDATE events SET eventValue = :x WHERE eventName = 'fruit' AND eventDbId = :name AND femaleObsUnitDbId = :female AND maleOBsUnitDbId = :male")
    fun updateFruit(name: String, x: Int, female: String, male: String)

    @Query("UPDATE events SET eventValue = :x WHERE eventName = 'seed' AND eventDbId = :name AND femaleObsUnitDbId = :female AND maleOBsUnitDbId = :male")
    fun updateSeed(name: String, x: Int, female: String, male: String)

    @Query("SELECT * FROM events")
    fun getAll(): LiveData<List<Events>>

    @Query("SELECT * FROM events WHERE events.eventName = 'flower'")
    fun getCrosses(): LiveData<List<Events>>

    @Query("SELECT * FROM events as e WHERE e.eventDbId = :name and e.eventName = 'flower' LIMIT 1")
    fun getPollination(name: String): LiveData<Events>

    @Query("SELECT * FROM events as e WHERE e.eventDbId = :name and e.eventName = 'fruit' LIMIT 1")
    fun getHarvest(name: String): LiveData<Events>

    @Query("SELECT * FROM events as e WHERE e.eventDbId = :name and e.eventName = 'seed' LIMIT 1")
    fun getThresh(name: String): LiveData<Events>
}