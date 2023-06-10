package com.udacity.location_reminders.view.reminders_list

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.location_reminders.R
import com.udacity.location_reminders.data.FakeDataSource
import com.udacity.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.data.local.FakeRemindersLocalRepository
import com.udacity.location_reminders.domain.FakeUser
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.util.DataBindingIdlingResource
import com.udacity.location_reminders.util.monitorFragment
import com.udacity.location_reminders.utils.condition_watcher.ConditionWatcher
import com.udacity.location_reminders.utils.EspressoIdlingResource
import com.udacity.location_reminders.utils.condition_watcher.Instruction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
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
    private val fakeRepository: FakeRemindersLocalRepository by inject()
    private val user: UserInterface by inject()
    private val dataBindingIdlingResource: DataBindingIdlingResource by inject()

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
                single { DataBindingIdlingResource() }
                single { FakeDataSource() }
                single<UserInterface> { FakeUser() }
                single<ReminderDataSource> {
                    FakeRemindersLocalRepository(
                        get()
                    )
                }
                single { get<ReminderDataSource>() as FakeRemindersLocalRepository }
            })
        }
        with(IdlingRegistry.getInstance()) {
            register(EspressoIdlingResource.countingIdlingResource)
            register(dataBindingIdlingResource)
        }
        user.onSignInResult(fakeDataSource.user1id)
    }

    @After
    fun shutdown() {
        with(IdlingRegistry.getInstance()) {
            unregister(dataBindingIdlingResource)
            unregister(EspressoIdlingResource.countingIdlingResource)
        }
    }

    // region DONE: test the navigation of the fragments.
    @Test
    fun reminderListOnFirstReminderClick() = runTest {
        // GIVEN the db contains 2 reminders for the logged in user
        fakeRepository.saveReminder(fakeDataSource.reminder1)
        fakeRepository.saveReminder(fakeDataSource.reminder2)

        FragmentScenario.launchInContainer(
            ReminderListFragment::class.java,
            Bundle(),
            R.style.AppTheme
        ).use { scenario ->
            @Suppress("UNCHECKED_CAST")
            dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<Fragment>)

            val navController = Mockito.mock(NavController::class.java)
            scenario.onFragment { fragment ->
                Navigation.setViewNavController(fragment.view!!, navController)
            }
            // WHEN the user clicks on the first reminder in the list
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun getDescription(): String { return "wait reminders reflect to the list" }
                override fun checkCondition(): Boolean {
                    var result = false
                    scenario.onFragment() {
                        result = it.view?.findViewById<RecyclerView?>(R.id.remindersRecyclerView)?.childCount == 2
                    }
                    return result
                }
            })
            val reminder = ReminderDataItem.fromReminderDTO(fakeDataSource.reminder1)
            onView(withId(R.id.remindersRecyclerView))
                .perform(
                    RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                        hasDescendant(withText(reminder.title)),
                        click()
                    )
                )

            // THEN the app navigates to save reminder screen with the right reminder data
            Mockito.verify(navController).navigate(
                ReminderListFragmentDirections.toSaveReminder(reminder)
            )
        }
    }

    @Test
    fun reminderListOnSecondReminderClick() = runTest {
        // GIVEN the db contains 2 reminders for the logged in user
        fakeRepository.saveReminder(fakeDataSource.reminder1)
        fakeRepository.saveReminder(fakeDataSource.reminder2)

        FragmentScenario.launchInContainer(
            ReminderListFragment::class.java,
            Bundle(),
            R.style.AppTheme
        ).use { scenario ->
            @Suppress("UNCHECKED_CAST")
            dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<Fragment>)

            val navController = Mockito.mock(NavController::class.java)
            scenario.onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }

            // WHEN the user clicks on the second reminder in the list
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun getDescription(): String { return "checking snackbar" }
                override fun checkCondition(): Boolean {
                    var result = false
                    scenario.onFragment() {
                        result = it.view?.findViewById<RecyclerView?>(R.id.remindersRecyclerView)?.childCount == 2
                    }
                    return result
                }
            })
            val reminder = ReminderDataItem.fromReminderDTO(fakeDataSource.reminder2)
            onView(withId(R.id.remindersRecyclerView))
                .perform(
                    RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                        hasDescendant(withText(reminder.title)),
                        click()
                    )
                )

            // THEN the app navigates to save reminder screen with the right reminder data
            Mockito.verify(navController).navigate(
                ReminderListFragmentDirections.toSaveReminder(reminder)
            )
        }
    }

    @Test
    fun reminderListOnNewItemClick() = runTest {
        // GIVEN user 1 is logged in
        // nothing to do, it's the standard setup
        // make ReminderDataItem id deterministic
        ReminderDataItem.fixUUID(ReminderDataItem.generateRandomUUID())
        FragmentScenario.launchInContainer(
            ReminderListFragment::class.java,
            Bundle(),
            R.style.AppTheme
        ).use { scenario ->
            @Suppress("UNCHECKED_CAST")
            dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<Fragment>)

            val navController = Mockito.mock(NavController::class.java)
            scenario.onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }

            // WHEN the user clicks for adding a new reminder
            onView(withId(R.id.addReminderFAB))
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
    }

    // endregion

    // region DONE: test the displayed data on the UI.

    @Test
    fun reminderListOnNoReminderData() = runTest {
        // GIVEN the logged in user has no reminders registered
        fakeRepository.deleteAllReminders(fakeDataSource.user1id)
        FragmentScenario.launchInContainer(
            ReminderListFragment::class.java,
            Bundle(),
            R.style.AppTheme
        ).use { scenario ->
            @Suppress("UNCHECKED_CAST")
            dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<Fragment>)

            val navController = Mockito.mock(NavController::class.java)
            scenario.onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }

            // THEN the No Data icon is displayed
            ConditionWatcher.waitForCondition(object : Instruction() {
                override fun getDescription(): String { return "checking snackbar" }
                override fun checkCondition(): Boolean {
                    var result = false
                    scenario.onFragment() {
                        result = it.view?.findViewById<View>(R.id.noDataTextView) != null
                    }
                    return result
                }
            })

            onView(withId(R.id.noDataTextView))
                .check { view, _ ->
                    MatcherAssert.assertThat(view.isVisible, CoreMatchers.`is`(true))
                }
        }
    }

    @Test
    fun reminderListWithTwoReminders() = runTest {
        // GIVEN the logged in user has 2 reminders
        FragmentScenario.launchInContainer(
            ReminderListFragment::class.java,
            Bundle(),
            R.style.AppTheme
        ).use { scenario ->
            @Suppress("UNCHECKED_CAST")
            dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<Fragment>)

            val navController = Mockito.mock(NavController::class.java)
            scenario.onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }

            // THEN recyclerview displays two reminders
            onView(withId(R.id.remindersRecyclerView))
                .check { view, _ ->
                    view as RecyclerView
                    MatcherAssert.assertThat(view.childCount, CoreMatchers.`is`(2))
                }
            // AND display the title of the logged in user's reminders
            onView(withText(fakeDataSource.reminder1.title))
                .check { view, _ ->
                    MatcherAssert.assertThat(view.isVisible, CoreMatchers.`is`(true))
                }
            onView(withText(fakeDataSource.reminder2.title))
                .check { view, _ ->
                    MatcherAssert.assertThat(view.isVisible, CoreMatchers.`is`(true))
                }
        }
    }

    @Test
    fun reminderListWithOneReminder() = runTest {
        // GIVEN the logged in user has 1 reminder
        user.onSignInResult(fakeDataSource.user2id)
        FragmentScenario.launchInContainer(
            ReminderListFragment::class.java,
            Bundle(),
            R.style.AppTheme
        ).use { scenario ->
            @Suppress("UNCHECKED_CAST")
            dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<Fragment>)


            val navController = Mockito.mock(NavController::class.java)
            scenario.onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }

            // THEN recyclerview displays two reminders
            onView(withId(R.id.remindersRecyclerView))
                .check { view, _ ->
                    view as RecyclerView
                    MatcherAssert.assertThat(view.childCount, CoreMatchers.`is`(1))
                }
            // AND display the title of the logged in user's reminders
            onView(withText(fakeDataSource.reminder3.title))
                .check { view, _ ->
                    MatcherAssert.assertThat(view.isVisible, CoreMatchers.`is`(true))
                }
        }
    }

    // endregion

    // region DONE: add testing for the error messages.

    @Test
    fun reminderListErrorLoading() = runTest {
        // GIVEN the the database is somehow unavailable
        fakeRepository.simulateDatabaseError = true
        user.logout()
        user.onSignInResult(fakeDataSource.user1id)
        FragmentScenario.launchInContainer(
            ReminderListFragment::class.java,
            Bundle(),
            R.style.AppTheme
        ).use { scenario ->
            @Suppress("UNCHECKED_CAST")
            dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<Fragment>)

            val navController = Mockito.mock(NavController::class.java)
            scenario.onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }

            // THEN recyclerview displays two reminders
            ConditionWatcher.waitForCondition(object : Instruction() {

                // Solution here composed mixing proposals found at
                // https://stackoverflow.com/questions/33111882/testing-snackbar-show-with-espresso/33245290
                // https://kamilkrzyk.medium.com/hello-6771c817f4e1
                // https://medium.com/azimolabs/wait-for-it-idlingresource-and-conditionwatcher-602055f32356
                // https://github.com/AzimoLabs/ConditionWatcher/tree/master

                override fun getDescription(): String {
                    return "checking snackbar"
                }

                override fun checkCondition(): Boolean {
                    var result = false

                    scenario.onFragment() {
                        val snackBar = it.activity?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        result = snackBar != null
                    }
                    return result
                }
            })
            onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText(FakeRemindersLocalRepository.MSG_DB_ERROR)))
            // AND the No Data icon is displayed
            onView(withId(R.id.noDataTextView))
                .check { view, _ ->
                    MatcherAssert.assertThat(view.isVisible, CoreMatchers.`is`(true))
                }
        }
    }

    // endregion
}