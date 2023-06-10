package com.udacity.location_reminders.view

import android.app.Application
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.udacity.location_reminders.R
import com.udacity.location_reminders.authentication.data.User
import com.udacity.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.data.local.LocalDB
import com.udacity.location_reminders.data.local.RemindersLocalRepository
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.test_utils.getOrAwaitValue
import com.udacity.location_reminders.utils.Constants
import com.udacity.location_reminders.utils.condition_watcher.ConditionWatcher
import com.udacity.location_reminders.utils.condition_watcher.Instruction
import com.udacity.location_reminders.view.authentication.AuthenticationViewModel
import com.udacity.location_reminders.view.reminders_list.ReminderDataItem
import com.udacity.location_reminders.view.reminders_list.RemindersListViewModel
import com.udacity.location_reminders.view.save_reminder.SaveReminderViewModel
import com.udacity.location_reminders.view.save_reminder.select_reminder_location.SelectLocationFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    companion object {
        const val TEST_USER_UID = "some-user-ui"
    }

    private val repository: ReminderDataSource by inject()
    private val user: UserInterface by inject()
    private lateinit var appContext: Application


    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )


    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get(),
                    get() as ReminderDataSource
                )
            }
            viewModel {
                AuthenticationViewModel(Application(), get(), get())
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get(),
                    get() as ReminderDataSource
                )
            }
            single {
                mock(FirebaseAuth::class.java)
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
            single<UserInterface> { User(get()) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders(TEST_USER_UID)
        }
    }

    //    DONE: add End to End testing to the app
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun firebaseCustomUI() = runTest {
        // GIVEN no user is logged in
        val auth: FirebaseAuth by inject()
        val fUser: FirebaseUser = mock(FirebaseUser::class.java)
        Mockito.`when`(auth.currentUser).then { fUser }
        user.logout()
        verify(auth).currentUser
        // THEN Registration is requested and login enters firebase custom login screen
        ActivityScenario.launch(RemindersActivity::class.java).use {
            onView(withId(R.id.login)).perform(click())
            onView(allOf(withId(R.id.emailSignInButton), isDisplayed()))
            onView(allOf(withId(R.id.googleSignInButton), isDisplayed()))
        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun remindersE2ETest() = runTest {
        // GIVEN user1 is logged in
        // manually perform login
        // bypass firebase internal screens as system popup cannot be manipulated by Espresso
        user.userUniqueId.getOrAwaitValue {
            // do nothing
        }
        user.onSignInResult(TEST_USER_UID)
        // wait propagation of user information
        user.userUniqueId.getOrAwaitValue()
        // wait with long timeout
        waitForCondition("waiting main screen", timeoutLimit = 15000) {
            user.isAuthenticated
        }

        // THEN Registration is requested and login enters firebase custom login screen
        // relaunch activity after login
        ActivityScenario.launch(RemindersActivity::class.java).use { scenario ->
            // Check main screen displays No Data indicator, logout and Add option
            onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
            onView(withId(R.id.logout)).check(matches(isDisplayed()))
            onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))

            val reminder1 = ReminderDataItem(
                userUniqueId = TEST_USER_UID,
                title = "Reminder 01 Title",
                description = "Reminder 01 Description",
                radius = 200,
                location = null,
                longitude = null,
                latitude = null
            )
            val reminder2 = ReminderDataItem(
                userUniqueId = TEST_USER_UID,
                title = "Reminder 02 Title",
                description = "Reminder 02 Description",
                radius = 200,
                location = null,
                longitude = null,
                latitude = null
            )
            val reminder1Edited = ReminderDataItem(
                userUniqueId = TEST_USER_UID,
                title = "Reminder 01 Title Edited",
                description = "Reminder 01 Description Edited",
                radius = 300,
                location = null,
                longitude = null,
                latitude = null
            )
            val reminder2Edited = ReminderDataItem(
                userUniqueId = TEST_USER_UID,
                title = "Reminder 02 Title Edited",
                description = "Reminder 02 Description Edited",
                radius = 115,
                location = null,
                longitude = null,
                latitude = null
            )
            addReminderTest(scenario, reminder1)
            addReminderTest(scenario, reminder2, 2)
            editReminderTest(scenario, reminder1, reminder1Edited, 2)
            editReminderTest(scenario, reminder2, reminder2Edited, 2)
        }
    }

    private fun addReminderTest(
        scenario: ActivityScenario<RemindersActivity>,
        r: ReminderDataItem,
        expectedRemindersCount: Int = 1
    ) {

        // Click to add new reminder
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Save Reminder Fragment - Check initial conditions
        onView(allOf(withId(R.id.reminderTitle))).check(matches(withText("")))
        onView(allOf(withId(R.id.reminderDescription))).check(matches(withText("")))
        onView(withId(R.id.selectedLocation)).check(
            matches(
                withText(
                    appContext.getString(
                        R.string.no_location_selected
                    )
                )
            )
        )

        // Input reminder title and description
        onView(withId(R.id.reminderTitle)).perform(replaceText(r.title))
        onView(withId(R.id.reminderDescription)).perform(
            replaceText(r.description),
            closeSoftKeyboard()
        )

        // Attempt to save the reminder without a selected location
        onView(allOf(withId(R.id.saveReminder), isDisplayed())).perform(click())

        // WHEN the user clicks on the first reminder in the list
        verifySnackBar(scenario, appContext.getString(R.string.select_location))

        // Select location
        onView(withId(R.id.selectLocation)).perform(click())

        // Check screen initial conditions
        onView(withId(R.id.placeName)).check(matches(withText("")))
        onView(withId(R.id.radiusExplanation)).check(
            matches(
                withText(
                    appContext.getString(
                        R.string.geofence_radius_description,
                        Constants.DEFAULT_ROUND_GEOFENCE_RADIUS
                    )
                )
            )
        )

        // Change radius
        onView(withId(R.id.radiusSeekBar)).perform(setProgress(r.radius!!))
        onView(withId(R.id.radiusExplanation)).check(
            matches(
                withText(
                    appContext.getString(
                        R.string.geofence_radius_description,
                        r.radius
                    )
                )
            )
        )
        // set a marker
        // https://stackoverflow.com/questions/29924564/using-espresso-to-unit-test-google-maps
        onView(withContentDescription("Google Map")).perform(click())
        waitForCondition("wait location name to become available") {
            r.location = getText(withId(R.id.placeName))
            !r.location.isNullOrEmpty()
        }
        MatcherAssert.assertThat(r.location.isNullOrEmpty(), `is`(false))
        onView(withId(R.id.placeName)).check(matches(withText(r.location)))
        val geofenceDescription = buildGeofenceDescription(r)
        // save the location
        waitForElement<TextView>(scenario, "wait save button to be ready", R.id.save)
        waitForCondition("wait for location live data", timeoutLimit = 30000) {
            var result = false
            scenario.onActivity {
                it.findViewById<TextView>(R.id.save)?.let { view ->
                    val vm = view.findFragment<SelectLocationFragment>().vm
                    result = !vm.locationReminder.value?.location.isNullOrEmpty()
                }
            }
            result
        }

        // Click Save sometimes will not transition. It doesn't seem to happen outside of
        // tests
        waitForCondition("retry click if no transition", timeoutLimit = 30000) {
            onView(withId(R.id.save)).perform(click())
            waitForElement<TextView>(
                scenario,
                "wait return from save location screen",
                R.id.selectLocation,
                timeoutLimit = 1000,
                skipException = true
            )
            scenarioActivityElementExists<TextView>(scenario, R.id.selectLocation)
        }

        waitForElement<TextView>(
            scenario,
            "wait return from save location screen",
            R.id.selectLocation
        )
        // confirm name of selected place is carried over
        verifyReminderData(r, geofenceDescription)
        // Save the reminder
        waitForCondition("retry click if no transition", timeoutLimit = 30000) {
            onView(withId(R.id.saveReminder)).perform(click())
            waitForElement<TextView>(
                scenario,
                "wait return from save location screen",
                R.id.addReminderFAB,
                timeoutLimit = 1000,
                skipException = true
            )
            scenarioActivityElementExists<TextView>(scenario, R.id.addReminderFAB)
        }

        // THEN recyclerview displays one reminder
        onView(withId(R.id.remindersRecyclerView))
            .check { view, _ ->
                view as RecyclerView
                MatcherAssert.assertThat(view.childCount, `is`(expectedRemindersCount))
            }
        // AND display the title of the logged in user's reminders
        verifyReminderData(r, geofenceDescription)
    }

    private fun verifySnackBar(scenario : ActivityScenario<RemindersActivity>, message : String) {
        waitForElement<TextView>(
            scenario,
            "wait for the snack bar",
            com.google.android.material.R.id.snackbar_text
        )
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(message)))
    }

    private fun verifyReminderData(
        r: ReminderDataItem,
        geofenceDescription: String = buildGeofenceDescription(r)
    ) {
        // AND display the title of the logged in user's reminders
        onView(withText(r.title)).check(matches(isDisplayed()))
        onView(withText(r.description)).check(matches(isDisplayed()))
        onView(withText(geofenceDescription)).check(matches(isDisplayed()))
    }

    private fun editReminderTest(
        scenario: ActivityScenario<RemindersActivity>,
        originalR: ReminderDataItem,
        newR: ReminderDataItem,
        expectedRemindersCount: Int
    ) {
        // THEN recyclerview displays one reminder
        onView(withId(R.id.remindersRecyclerView))
            .check { view, _ ->
                view as RecyclerView
                MatcherAssert.assertThat(view.childCount, `is`(expectedRemindersCount))
            }
        // AND display the title of the logged in user's reminders
        var geofenceDescription = buildGeofenceDescription(originalR)

        // Click to edit the reminder
        onView(withText(originalR.title)).perform(click())

        // Confirm switched to edit screen
        onView(withId(R.id.saveReminder)).check(matches(isDisplayed()))
        // Verify reminder data is carried over
        verifyReminderData(originalR, geofenceDescription)

        // Change reminder data title and description
        onView(withId(R.id.reminderTitle)).perform(replaceText(newR.title))
        onView(withId(R.id.reminderDescription)).perform(
            replaceText(newR.description),
            closeSoftKeyboard()
        )

        // Select location
        onView(withId(R.id.selectLocation)).perform(click())

        // Check screen initial conditions
        onView(withId(R.id.placeName)).check(matches(withText(originalR.location)))
        onView(withId(R.id.radiusExplanation)).check(
            matches(
                withText(
                    appContext.getString(
                        R.string.geofence_radius_description,
                        originalR.radius
                    )
                )
            )
        )

        // Change radius
        onView(withId(R.id.radiusSeekBar)).perform(setProgress(newR.radius!!))
        onView(withId(R.id.radiusExplanation)).check(
            matches(
                withText(
                    appContext.getString(
                        R.string.geofence_radius_description,
                        newR.radius
                    )
                )
            )
        )
        // set a marker
        // https://stackoverflow.com/questions/29924564/using-espresso-to-unit-test-google-maps
        onView(withContentDescription("Google Map")).perform(click())
        waitForCondition("wait location name to become available") {
            newR.location = getText(withId(R.id.placeName))
            !newR.location.isNullOrEmpty()
        }
        MatcherAssert.assertThat(newR.location.isNullOrEmpty(), `is`(false))
        onView(withId(R.id.placeName)).check(matches(withText(newR.location)))
        geofenceDescription = buildGeofenceDescription(newR)
        // save the location
        waitForElement<TextView>(scenario, "wait save button to be ready", R.id.save)
        waitForCondition("wait for location live data", timeoutLimit = 30000) {
            var result = false
            scenario.onActivity {
                it.findViewById<TextView>(R.id.save)?.let { view ->
                    val vm = view.findFragment<SelectLocationFragment>().vm
                    result = !vm.locationReminder.value?.location.isNullOrEmpty()
                }
            }
            result
        }

        // Click Save sometimes will not transition. It doesn't seem to happen outside of
        // tests
        waitForCondition("retry click if no transition", timeoutLimit = 30000) {
            onView(withId(R.id.save)).perform(click())
            waitForElement<TextView>(
                scenario,
                "wait return from save location screen",
                R.id.selectLocation,
                timeoutLimit = 1000,
                skipException = true
            )
            scenarioActivityElementExists<TextView>(scenario, R.id.selectLocation)
        }

        waitForElement<TextView>(
            scenario,
            "wait return from save location screen",
            R.id.selectLocation
        )
        // confirm name of selected place is carried over
        verifyReminderData(newR, geofenceDescription)
        // Save the reminder
        onView(withId(R.id.saveReminder)).perform(click())
        waitForElement<FloatingActionButton>(
            scenario,
            "wait return from save reminder screen",
            R.id.addReminderFAB
        )
        verifyReminderData(newR, geofenceDescription)
    }

    private fun buildGeofenceDescription(r: ReminderDataItem): String {
        return appContext.getString(
            R.string.geofence_location_description,
            r.radius, r.location
        )
    }

    private fun <T : View> waitForElement(
        scenario: ActivityScenario<RemindersActivity>,
        description: String,
        @IdRes id: Int,
        timeoutLimit: Int = 30000,
        skipException: Boolean = true
    ) {
        waitForCondition(description, timeoutLimit = timeoutLimit, skipException) {
            scenarioActivityElementExists<T>(scenario, id)
        }
    }

    private fun <T : View> scenarioActivityElementExists(
        scenario: ActivityScenario<RemindersActivity>,
        id: Int
    ): Boolean {
        var result = false
        scenario.onActivity {
            val subject = it.findViewById<T>(id)
            result = subject != null
        }
        return result
    }

    // obtained from: https://stackoverflow.com/questions/23659367/espresso-set-seekbar
    private fun setProgress(progress: Int): ViewAction {
        return object : ViewAction {
            override fun perform(uiController: UiController?, view: View) {
                val seekBar = view as SeekBar
                seekBar.progress = progress
            }

            override fun getDescription(): String {
                return "Set a progress on a SeekBar"
            }

            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(SeekBar::class.java)
            }
        }
    }

    // inspired on: https://stackoverflow.com/questions/23381459/how-to-get-text-from-textview-using-espresso
    private fun getText(matcher: Matcher<View?>?): String? {
        var result: String? = null
        onView(matcher).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(TextView::class.java)
            }

            override fun getDescription(): String {
                return "getting text from a TextView"
            }

            override fun perform(uiController: UiController, view: View) {
//                val subject =  //Save, because of check in getConstraints()
                view as TextView
                result = view.text.toString()
            }
        })
        return result
    }

    private fun waitForCondition(
        description: String,
        timeoutLimit: Int = 5000,
        skipException: Boolean = false,
        conditionBlock: () -> Boolean
    ) {
        val instruction = object : Instruction() {

            override fun getDescription(): String {
                return description
            }

            override fun checkCondition(): Boolean {
                return conditionBlock()
            }
        }
        if (skipException) instruction.disableThrowingExceptions()
        ConditionWatcher.waitForCondition(instruction, timeoutLimit)
    }

}
