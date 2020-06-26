package org.phenoapps.intercross.test

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.dao.EventsDao
import java.io.IOException

class DatabaseTests {

    class EventsInsertTest {

        private lateinit var eventsDao: EventsDao

        private lateinit var db: IntercrossDatabase

        @Before
        fun createDb() {

            val context = ApplicationProvider.getApplicationContext<Context>()

            db = Room.inMemoryDatabaseBuilder(
                    context, IntercrossDatabase::class.java).build()

            eventsDao = db.eventsDao()
        }

        @After
        @Throws(IOException::class)
        fun closeDb() {
            db.close()
        }

        @Test
        @Throws(Exception::class)
        fun insertEvents() {

            val inputEvents = TestUtil.createEvent(2048)

            eventsDao.insert(*inputEvents.toTypedArray())

            ///assertThat(byName.get(0), equalTo(user))
        }
    }

}
