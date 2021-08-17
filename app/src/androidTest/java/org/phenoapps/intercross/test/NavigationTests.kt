package org.phenoapps.intercross.test

import android.content.Intent
import android.view.Gravity
import androidx.navigation.NavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.phenoapps.intercross.activities.MainActivity
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

    private fun openNavDrawer() {

        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open())
    }

    @Test
    fun testEventsToParentsFragment() {

        // Create a TestNavHostController
        val navController = mock(NavController::class.java)

        activityRule.launchActivity(Intent())

//        // Create a graphical FragmentScenario for the TitleScreen
//        val titleScenario = launchFragmentInContainer<EventsFragment>()
//
//        // Set the NavController property on the fragment
//        titleScenario.onFragment { fragment ->
//            Navigation.setViewNavController(fragment.requireView(), navController)
//        }

        openNavDrawer()

        // Verify that performing a click changes the NavController’s state
        onView(withId(R.id.nvView))
                .perform(NavigationViewActions.navigateTo(R.id.action_nav_parents))

        assert(navController.currentDestination?.id == R.id.parents_fragment)

    }

    @Test
    fun testEventsFragment() {

        // Type text and then press the button.
        onView(withId(R.id.firstText))
                .perform(typeText(femaleString), closeSoftKeyboard())

        onView(withId(R.id.secondText))
                .perform(typeText(maleString), closeSoftKeyboard())

        onView(withId(R.id.firstText))
                .perform(typeText(crossString), closeSoftKeyboard())

        onView(withId(R.id.saveButton)).perform(click())

//        // Check that the text was changed.
//        onView(withId(R.id.textToBeChanged))
//                .check(matches(withText(stringToBetyped)))

    }
}
