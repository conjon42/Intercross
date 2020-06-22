package org.phenoapps.intercross

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.phenoapps.intercross.MainActivity
import org.phenoapps.intercross.R
import java.util.*

class NavigationTests {

    private lateinit var maleString: String
    private lateinit var femaleString: String
    private lateinit var crossString: String

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity>
            = ActivityTestRule(MainActivity::class.java)
    @Before
    fun initValidString() {

        maleString = UUID.randomUUID().toString()

        femaleString = UUID.randomUUID().toString()

        crossString = UUID.randomUUID().toString()
    }

    @Test
    fun testEventsFragment() {

//        // Type text and then press the button.
//        onView(withId(R.id.firstText))
//                .perform(typeText(femaleString), closeSoftKeyboard())
//
//        // Type text and then press the button.
//        onView(withId(R.id.secondText))
//                .perform(typeText(maleString), closeSoftKeyboard())
//
//        // Type text and then press the button.
//        onView(withId(R.id.firstText))
//                .perform(typeText(crossString), closeSoftKeyboard())
//
//        onView(withId(R.id.saveButton)).perform(click())
//
////        // Check that the text was changed.
////        onView(withId(R.id.textToBeChanged))
////                .check(matches(withText(stringToBetyped)))

    }
}
