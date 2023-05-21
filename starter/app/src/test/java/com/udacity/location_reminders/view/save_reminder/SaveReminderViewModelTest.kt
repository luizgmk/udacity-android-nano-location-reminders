package com.udacity.location_reminders.view.save_reminder

import android.app.Application
import androidx.lifecycle.testing.TestLifecycleOwner
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.concurrent.TimeoutException
import kotlin.test.assertFailsWith

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SaveReminderViewModelTest : AutoCloseKoinTest() {

    //DONE: provide testing to the SaveReminderView and its live data objects

    @Mock
    private lateinit var mockApp: Application

    private lateinit var mockedDataSource: ReminderDataSource
    private lateinit var fakeDataSource: FakeDataSource

    private val user: UserInterface by inject()
    private val vm: SaveReminderViewModel by inject()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // region setup

    @Before
    fun startTests() {
        // Given a mocked Context injected into the object under test...
        mockApp = mock {
            on { getString(R.string.no_location_selected) } doReturn "no_location_selected"
            on { getString(R.string.geofence_location_description, "100", "Some place 01" ) } doReturn "Within 100m from Some place 01"
            on { getString(R.string.select_location) } doReturn "select_location"
            on { getString(R.string.reminder_saved) } doReturn "reminder_saved"
        }
        // Unfortunately, couldn't figure out how to prevent Koin from being already started
        // So stopping Koin at start to prevent exception
        stopKoin()

        GlobalContext.startKoin {
            modules(module {
                single<ReminderDataSource> { fakeDataSource }
                single<UserInterface> { UserTest() }
                single { SaveReminderViewModel(mockApp, get(), get()) }
            })
        }
        fakeDataSource = FakeDataSource()
        user.onSignInResult(fakeDataSource.user1id)
    }

    // endregion setup

    // region livedata tests

    // original reminder data set when started editing
    // also updated with final data after user confirmation
    // DONE: covered by unit tests of viewModel's methods
    // val reminder = MutableLiveData<ReminderDataItem?>()

    // acts as a cursor that points to the latest user's selection
    // when new reminder, it starts as null
    // when editing, it starts with same location as the original reminder
    // it's updated as the user clicks on the map
    // DONE: covered by unit tests of viewModel's methods
    // val locationReminder = MutableLiveData<ReminderDataItem?>()

    // represents the user radius selection while editing
    // starts with default radius value when new and with reminder's radius when editing
    // DONE: covered by unit tests of viewModel's methods
    // val roundGeofenceRadiusSelection = MutableLiveData(Constants.DEFAULT_ROUND_GEOFENCE_RADIUS)

    // it's SingleLiveEvent that captures the action of the user pressing "save"
    // DONE: covered by unit tests of viewModel's methods
    // val locationSaved = SingleLiveEvent<Boolean>()

    // marker description displaying geo-coordinates
    // val geofenceLocationDescription: LiveData<String>
    @Test
    fun geofenceLocationDescriptionNoLocationSelected() = runTest {
        // GIVEN no location is selected
        val reminder = ReminderDataItem.fromReminderDTO(fakeDataSource.reminder1)
        reminder.location = null
        vm.editReminder(reminder)
        // WHEN attempting to save the location
        vm.reminder.getOrAwaitValue()
        val message = vm.geofenceLocationDescription.getOrAwaitValue()
        // THEN contains proper description for display
        MatcherAssert.assertThat(
            message,
            CoreMatchers.`is`(mockApp.getString(R.string.no_location_selected))
        )
    }

    @Test
    fun geofenceLocationDescriptionWithLocationSelected() {
        // GIVEN location is selected
        val reminder = ReminderDataItem.fromReminderDTO(fakeDataSource.reminder1)
        vm.editReminder(reminder)
        // WHEN attempting to save the location
        vm.reminder.getOrAwaitValue()
        val message = vm.geofenceLocationDescription.getOrAwaitValue()
        // THEN contains proper description for display
        MatcherAssert.assertThat(
            message,
            CoreMatchers.`is`(mockApp.getString(
                R.string.geofence_location_description,
                reminder.radius.toString(),
                reminder.location
            ))
        )
    }

    // endregion

    // region Methods test
    @Test
    fun editReminder() {
        // GIVEN a reminder
        val reminder = ReminderDataItem.fromReminderDTO(fakeDataSource.reminder1)
        // WHEN editing the reminder is requested
        vm.editReminder(reminder)
        // THEN main reminder data, current reminder selection and radius selection are
        // properly updated
        vm.reminder.getOrAwaitValue()
        vm.locationReminder.getOrAwaitValue()
        vm.roundGeofenceRadiusSelection.getOrAwaitValue()
        MatcherAssert.assertThat(
            vm.reminder.value?.hashCode(), CoreMatchers.`is`(reminder.hashCode())
        )
        MatcherAssert.assertThat(
            vm.locationReminder.value?.hashCode(), CoreMatchers.`is`(reminder.hashCode())
        )
        MatcherAssert.assertThat(
            vm.roundGeofenceRadiusSelection.value, CoreMatchers.`is`(reminder.radius)
        )
    }

    @Test
    fun saveLocationWhenSelected() {
        // GIVEN a location is selected
        val reminder = ReminderDataItem.fromReminderDTO(fakeDataSource.reminder1)
        vm.editReminder(reminder)
        // WHEN attempting to save the location
        vm.saveLocation()
        // THEN locationSaved event is triggered to indicate user has confirmed its selection
        vm.locationSaved.getOrAwaitValue()
        MatcherAssert.assertThat(vm.locationSaved.value, CoreMatchers.`is`(true))
        // AND snackBar message is never sent
        assertFailsWith<TimeoutException> {
            vm.showSnackBar.getOrAwaitValue(1)
        }
    }

    @Test
    fun saveLocationWhenNotSelected() {
        // GIVEN no location is selected
        vm.onClear()
        MatcherAssert.assertThat(vm.locationSaved.value, CoreMatchers.`is`(nullValue()))
        // WHEN attempting to save the location
        vm.saveLocation()
        // THEN a snackBar is triggered to suggest the user to select a location
        vm.showSnackBar.getOrAwaitValue()
        MatcherAssert.assertThat(
            vm.showSnackBar.value, CoreMatchers.`is`(mockApp.getString(R.string.select_location))
        )
        // AND locationSaved remains null
        MatcherAssert.assertThat(vm.locationSaved.value, CoreMatchers.`is`(nullValue()))
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
            vm.locationReminder.value?.userUniqueId, CoreMatchers.`is`(user.userUniqueId.value)
        )
    }

    @Test
    fun saveReminder() = runTest {
        val expectedNewTitle = "saveReminderTest"
        val owner = TestLifecycleOwner()

        // GIVEN an edit session is ongoing with reminder data
        val reminder = ReminderDataItem.fromReminderDTO(fakeDataSource.reminder1)
        MatcherAssert.assertThat(reminder.title, CoreMatchers.not(expectedNewTitle))

        // WHEN reminder data is changed, open for editing and saveReminder is called
        reminder.title = expectedNewTitle
        vm.editReminder(reminder)

        var loaderSequence = 0
        var savedReminder: ReminderDTO? = null
        vm.showLoading.observe(owner) {
            loaderSequence += 1
            when (loaderSequence) {
                1 -> {
                    // first loader value set is true, before reminder is saved
                    MatcherAssert.assertThat(savedReminder, CoreMatchers.`is`(nullValue()))
                    MatcherAssert.assertThat(it, CoreMatchers.`is`(true))
                }
                2 -> {
                    // after reminder is saved sets loader display to false
                    MatcherAssert.assertThat(savedReminder, CoreMatchers.not(nullValue()))
                    MatcherAssert.assertThat(it, CoreMatchers.`is`(false))
                }
                else -> throw Exception("No 3rd value set expected")
            }
        }
        vm.saveReminder()

        // THEN a loader is displayed
        var showLoading = vm.showLoading.getOrAwaitValue()
        // double-check value before coroutine is run / synchronous flow
        MatcherAssert.assertThat(showLoading, CoreMatchers.`is`(true))
        // AND reminder is saved to the data source
        savedReminder = fakeDataSource.lastSavedReminder.getOrAwaitValue()
        MatcherAssert.assertThat(savedReminder, CoreMatchers.`is`(reminder.toReminderDTO()))

        // WHEN remainder of launch block finishes
        advanceUntilIdle()
        // THEN a toast is shown indicating success
        val showToast = vm.showToast.getOrAwaitValue()
        MatcherAssert.assertThat(
            showToast,
            CoreMatchers.`is`(mockApp.getString(R.string.reminder_saved))
        )
        // AND at time when toast has been set, loader events have finished
        MatcherAssert.assertThat(loaderSequence, CoreMatchers.`is`(2))
        // AND navigation back is called
        val navigationCommand = vm.navigationCommand.getOrAwaitValue()
        MatcherAssert.assertThat(navigationCommand, CoreMatchers.`is`(NavigationCommand.Back))
    }

    @Test
    fun validateEnteredDataWhenReminderIsNull() {
        // GIVEN reminder is null
        // THEN validation returns false
        MatcherAssert.assertThat(vm.validateEnteredData(), CoreMatchers.`is`(false))
    }

    @Test
    fun validateEnteredDataWithUserUidNull() {
        // GIVEN reminder has no user uid set
        val reminder = ReminderDataItem.fromReminderDTO(fakeDataSource.reminder1)
        reminder.userUniqueId = null
        vm.editReminder(reminder)
        // THEN validation returns false
        assertFailsWith<SecurityException> {
            vm.validateEnteredData()
        }
    }

    @Test
    fun validateEnteredDataWithDifferentUser() {
        // GIVEN reminder has a user uid different from currently logged in user
        MatcherAssert.assertThat(user.userUniqueId.value, CoreMatchers.`is`(fakeDataSource.user1id))
        val reminder = ReminderDataItem.fromReminderDTO(fakeDataSource.reminder1)
        reminder.userUniqueId = fakeDataSource.user2id
        vm.editReminder(reminder)
        // THEN validation returns false
        assertFailsWith<SecurityException> {
            vm.validateEnteredData()
        }
    }

    @Test
    fun validateEnteredDataWithRequiredFieldsMissing() {
        val reminder = ReminderDataItem.fromReminderDTO(fakeDataSource.reminder1)

        // GIVEN title is not set
        reminder.title = ""
        vm.editReminder(reminder)
        // THEN validation returns false with proper error message
        MatcherAssert.assertThat(vm.validateEnteredData(), CoreMatchers.`is`(false))
        MatcherAssert.assertThat(
            vm.showSnackBarInt.getOrAwaitValue(),
            CoreMatchers.`is`(R.string.err_enter_title)
        )

        // GIVEN location is not set
        reminder.title = "some title"
        reminder.location = ""
        vm.editReminder(reminder)
        // THEN validation returns false with proper error message
        MatcherAssert.assertThat(vm.validateEnteredData(), CoreMatchers.`is`(false))
        MatcherAssert.assertThat(
            vm.showSnackBarInt.getOrAwaitValue(),
            CoreMatchers.`is`(R.string.err_select_location)
        )

        // GIVEN radius is not set
        reminder.location = "Some location"
        reminder.radius = null
        vm.editReminder(reminder)
        // THEN validation returns false with proper error message
        MatcherAssert.assertThat(vm.validateEnteredData(), CoreMatchers.`is`(false))
        MatcherAssert.assertThat(
            vm.showSnackBarInt.getOrAwaitValue(),
            CoreMatchers.`is`(R.string.err_invalid_radius)
        )

        // GIVEN reminder is valid
        reminder.radius = 200
        vm.editReminder(reminder)
        // THEN validation returns true
        MatcherAssert.assertThat(vm.validateEnteredData(), CoreMatchers.`is`(true))
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
            vm.locationReminder.value?.userUniqueId, CoreMatchers.`is`(user.userUniqueId.value)
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
            vm.locationReminder.value?.userUniqueId, CoreMatchers.`is`("")
        )
    }

    // endregion

}