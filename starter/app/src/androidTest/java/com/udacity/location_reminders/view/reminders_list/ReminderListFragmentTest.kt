package com.udacity.location_reminders.view.reminders_list

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.location_reminders.R
import com.udacity.location_reminders.data.FakeDataSource
import com.udacity.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.data.local.FakeRemindersLocalRepository
import com.udacity.location_reminders.domain.FakeUser
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.test_utils.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

    private val fakeDataSource: FakeDataSource by inject()
    private val repo: FakeRemindersLocalRepository by inject()
    private val user: UserInterface by inject()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        stopKoin()

        GlobalContext.startKoin {
            modules(module {
                viewModel {
                    RemindersListViewModel(
                        Application(),
                        get(),
                        get()
                    )
                }
                single { FakeDataSource() }
                single<ReminderDataSource> { fakeDataSource }
                single<UserInterface> { FakeUser() }
                single { FakeRemindersLocalRepository(get(), mainCoroutineRule.dispatcher) }
            })
        }
        user.onSignInResult(fakeDataSource.user1id)
    }

    //    TODO: test the navigation of the fragments.

    @Test
    fun reminderListOnFirstReminderClick() = runTest {
        // GIVEN the db contains 2 reminders for the logged in user
        repo.saveReminder(fakeDataSource.reminder1)
        repo.saveReminder(fakeDataSource.reminder2)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN the user clicks on the first reminder in the list
        val reminder = ReminderDataItem.fromReminderDTO(fakeDataSource.reminder1)
        Espresso.onView(withId(R.id.remindersRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    ViewMatchers.hasDescendant(ViewMatchers.withText(reminder.title)),
                    click()
                )
            )

        // THEN the app navigates to save reminder screen with the right reminder data
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder(reminder)
        )
    }

    @Test
    fun reminderListOnSecondReminderClick() = runTest {
        // GIVEN the db contains 2 reminders for the logged in user
        repo.saveReminder(fakeDataSource.reminder1)
        repo.saveReminder(fakeDataSource.reminder2)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN the user clicks on the second reminder in the list
        val reminder = ReminderDataItem.fromReminderDTO(fakeDataSource.reminder2)
        Espresso.onView(withId(R.id.remindersRecyclerView))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    ViewMatchers.hasDescendant(ViewMatchers.withText(reminder.title)),
                    click()
                )
            )

        // THEN the app navigates to save reminder screen with the right reminder data
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder(reminder)
        )
    }

    @Test
    fun reminderListOnNewItemClick() = runTest {
        // GIVEN user 1 is logged in
        // nothing to do, it's the standard setup
        // make ReminderDataItem id deterministic
        ReminderDataItem.fixUUID(ReminderDataItem.generateRandomUUID())
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN the user clicks for adding a new reminder
        Espresso.onView(withId(R.id.addReminderFAB))
            .perform(
                click()
            )

        // THEN the app navigates to save reminder screen with the right reminder data
        Mockito.verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder(
                ReminderDataItem.getNewEmptyReminder(
                    fakeDataSource.user1id
                )
            )
        )

    }
    //    TODO: test the displayed data on the UI.
    //    TODO: add testing for the error messages.
}