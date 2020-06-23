package org.phenoapps.intercross.test

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.google.common.base.Predicates.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.phenoapps.intercross.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.dao.EventsDao
import org.phenoapps.intercross.data.models.Event
import java.io.IOException
import java.util.*

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
