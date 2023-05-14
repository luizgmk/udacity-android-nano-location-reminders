package com.udacity.location_reminders.view.save_reminder

import android.app.Activity
import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.location_reminders.R
import com.udacity.location_reminders.data.FakeDataSource
import com.udacity.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.data.dto.ReminderDTO
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.domain.UserTest
import com.udacity.location_reminders.test_utils.MainCoroutineRule
import com.udacity.location_reminders.test_utils.getOrAwaitValue
import com.udacity.location_reminders.view.base.NavigationCommand
import com.udacity.location_reminders.view.reminders_list.ReminderDataItem
import com.udacity.location_reminders.view.reminders_list.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.concurrent.TimeoutException
import kotlin.test.assertFailsWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : AutoCloseKoinTest() {

    //DONE: provide testing to the SaveReminderView and its live data objects

    private val repo: ReminderDataSource by inject()
    private val user: UserInterface by inject()
    private val vm: SaveReminderViewModel by inject()
    private lateinit var fakeRepo: FakeDataSource

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // region setup

    @Before
    fun startTests() {
        // Unfortunately, couldn't figure out how to prevent Koin from being already started
        // So stopping Koin at start to prevent exception
        stopKoin()
        GlobalContext.startKoin {
            modules(
                module {
                    single<ReminderDataSource> { FakeDataSource() }
                    single<UserInterface> { UserTest() }
                    single { SaveReminderViewModel(Application(), get(), get()) }
                })
        }
        fakeRepo = repo as FakeDataSource
        user.onSignInResult(fakeRepo.user1id)
    }

    // endregion setup

    // region livedata tests

    @Test
    fun liveDataReminder() {
        // original reminder data set when started editing
        // also updated with final data after user confirmation
    }

    @Test
    fun liveDataLocationReminder() {
        // acts as a cursor that points to the latest user's selection
        // when new reminder, it starts as null
        // when editing, it starts with same location as the original reminder
        // it's updated as the user clicks on the map
    }

    @Test
    fun liveDataRoundGeofenceRadiusSelection() {
        // represents the user radius selection while editing
        // starts with default radius value when new and with reminder's radius when editing
    }

    @Test
    fun liveDataLocationSaved() {
        // it's SingleLiveEvent that captures the action of the user pressing "save"
    }

    fun geofenceLocationDescription() {
        // marker description displaying geo-coordinates
    }

    // endregion

    // region Methods test
    @Test
    fun editReminder() {
        // GIVEN a reminder
        val reminder = ReminderDataItem.fromReminderDTO(fakeRepo.reminder1)
        // WHEN editing the reminder is requested
        vm.editReminder(reminder)
        // THEN main reminder data, current reminder selection and radius selection are
        // properly updated
        vm.reminder.getOrAwaitValue()
        vm.locationReminder.getOrAwaitValue()
        vm.roundGeofenceRadiusSelection.getOrAwaitValue()
        MatcherAssert.assertThat(
            vm.reminder.value?.hashCode(),
            CoreMatchers.`is`(reminder.hashCode())
        )
        MatcherAssert.assertThat(
            vm.locationReminder.value?.hashCode(),
            CoreMatchers.`is`(reminder.hashCode())
        )
        MatcherAssert.assertThat(
            vm.roundGeofenceRadiusSelection.value,
            CoreMatchers.`is`(reminder.radius)
        )
    }

    @Test
    fun saveLocation() {
        // GIVEN a location is selected
        val reminder = ReminderDataItem.fromReminderDTO(fakeRepo.reminder1)
        vm.editReminder(reminder)
        // WHEN attempting to save the location
        vm.saveLocation()
        // THEN locationSaved event is triggered to indicate user has confirmed its selection
        vm.locationSaved.getOrAwaitValue()
        MatcherAssert.assertThat(vm.locationSaved.value, CoreMatchers.`is`(true))

        // TODO: mock resources
        //        // AND GIVEN a location is NOT selected
//        vm.onClear()
//        // WHEN attempting to save the location
//        vm.saveLocation()
//        // THEN a snackbar is triggered to suggest the user to select a location
//        vm.showSnackBar.getOrAwaitValue()
//        MatcherAssert.assertThat(
//            vm.showSnackBar.value,
//            CoreMatchers.`is`(Activity().getString(R.string.select_location))
//        )
//        // AND location saved is never set
//        assertFailsWith<TimeoutException> { vm.locationSaved.getOrAwaitValue() }
//        MatcherAssert.assertThat(vm.locationSaved.value, CoreMatchers.`is`(false))
    }

    @Test
    fun onClear() {
        // WHEN onClear is called (mostly when user logout/login)
        vm.onClear()
        // THEN data is cleared to guarantee no data is kept when users switches
        vm.reminder.getOrAwaitValue()
        vm.locationReminder.getOrAwaitValue()
        MatcherAssert.assertThat(vm.reminder.value, CoreMatchers.`is`(nullValue()))
        MatcherAssert.assertThat(
            vm.locationReminder.value?.userUniqueId,
            CoreMatchers.`is`(user.userUniqueId.value)
        )
    }

    @Test
    fun saveReminder() {
        // TODO : mock resources and data source
        // GIVEN an edit session is ongoing with reminder data
        val reminder = ReminderDataItem.fromReminderDTO(fakeRepo.reminder1)
        vm.editReminder(reminder)
        // WHEN saveReminder is called
        vm.saveReminder()
        // THEN a loader is displayed
        vm.showLoading.getOrAwaitValue()
        MatcherAssert.assertThat(vm.showLoading.value, CoreMatchers.`is`(true))
        // AND the loader is dismissed after IO is completed
        vm.showLoading.getOrAwaitValue()
        MatcherAssert.assertThat(vm.showLoading.value, CoreMatchers.`is`(false))
        // AND a toast is shown indicating success
        vm.showToast.getOrAwaitValue()
        MatcherAssert.assertThat(vm.showToast.value, CoreMatchers.`is`("???"))
        // AND navigation back is called
        vm.navigationCommand.getOrAwaitValue()
        MatcherAssert.assertThat(vm.navigationCommand.value, CoreMatchers.`is`(NavigationCommand.Back))
    }

    @Test
    fun validateEnteredData() {
        // TODO test pending
    }

    @Test
    fun onLoginSuccessful() {
        // WHEN user logs in (before tests logs in an user)
        vm.authenticated.getOrAwaitValue()
        MatcherAssert.assertThat(vm.authenticated.value, CoreMatchers.`is`(true))

        // THEN on clear is called initializing cursor with current user
        vm.reminder.getOrAwaitValue()
        vm.locationReminder.getOrAwaitValue()
        MatcherAssert.assertThat(vm.reminder.value, CoreMatchers.`is`(nullValue()))
        MatcherAssert.assertThat(
            vm.locationReminder.value?.userUniqueId,
            CoreMatchers.`is`(user.userUniqueId.value)
        )
    }

    @Test
    fun onLogoutCompleted() {
        // WHEN user logs our (before tests logs in an user)
        user.logout()
        vm.authenticated.getOrAwaitValue()
        MatcherAssert.assertThat(vm.authenticated.value, CoreMatchers.`is`(false))

        // THEN on clear is called initializing cursor with an invalid user id
        vm.reminder.getOrAwaitValue()
        vm.locationReminder.getOrAwaitValue()
        MatcherAssert.assertThat(vm.reminder.value, CoreMatchers.`is`(nullValue()))
        MatcherAssert.assertThat(
            vm.locationReminder.value?.userUniqueId,
            CoreMatchers.`is`("")
        )
    }

    // endregion

}