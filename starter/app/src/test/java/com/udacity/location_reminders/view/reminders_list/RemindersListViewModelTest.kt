package com.udacity.location_reminders.view.reminders_list

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.location_reminders.data.FakeDataSource
import com.udacity.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.domain.UserTest
import com.udacity.location_reminders.test_utils.MainCoroutineRule
import com.udacity.location_reminders.test_utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest : AutoCloseKoinTest() {

    //DONE: provide testing to the RemindersListViewModel and its live data objects
    // Use a fake repository to be injected into the viewModel
    private val repo: ReminderDataSource by inject()
    private val user: UserInterface by inject()
    private val vm: RemindersListViewModel by inject()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun startTests() {
        stopKoin()
        startKoin {
            modules(
                module {
                    single<ReminderDataSource> { FakeDataSource() }
                    single<UserInterface> { UserTest() }
                    single { RemindersListViewModel(Application(), get(), get()) }
                })
        }
    }

    @Test
    fun loadRemindersWithNoLoggedInUser() = runTest {
        vm.loadReminders()
        vm.remindersList.getOrAwaitValue()

        MatcherAssert.assertThat(user.userUniqueId.value, CoreMatchers.`is`(nullValue()))
        MatcherAssert.assertThat(vm.remindersList.value?.size, CoreMatchers.`is`(0))
    }

    @Test
    fun loadRemindersWithUser1() = runTest {
        // Confirm no user logged in at start
        MatcherAssert.assertThat(user.userUniqueId.value, CoreMatchers.`is`(nullValue()))

        // Confirm no reminders loaded at start
        vm.loadReminders()
        vm.remindersList.getOrAwaitValue()
        MatcherAssert.assertThat(vm.remindersList.value?.size, CoreMatchers.`is`(0))

        // Login with user1
        val fakeRepo = repo as FakeDataSource
        user.onSignInResult(fakeRepo.user1id)
        user.userUniqueId.getOrAwaitValue()
        MatcherAssert.assertThat(user.userUniqueId.value, CoreMatchers.`is`(fakeRepo.user1id))

        // Verify correct reminders were loaded for user1
        vm.loadReminders()
        vm.remindersList.getOrAwaitValue()
        MatcherAssert.assertThat(vm.remindersList.value?.size, CoreMatchers.`is`(2))
        MatcherAssert.assertThat(
            vm.remindersList.value?.first()?.id,
            CoreMatchers.`is`(fakeRepo.reminder1.id)
        )
        MatcherAssert.assertThat(
            vm.remindersList.value?.last()?.id,
            CoreMatchers.`is`(fakeRepo.reminder2.id)
        )
    }

    @Test
    fun loadRemindersWithUser2() = runTest {
        // Confirm no user logged in at start
        MatcherAssert.assertThat(user.userUniqueId.value, CoreMatchers.`is`(nullValue()))

        // Confirm no reminders loaded at start
        vm.loadReminders()
        vm.remindersList.getOrAwaitValue()
        MatcherAssert.assertThat(vm.remindersList.value?.size, CoreMatchers.`is`(0))

        // Login with user2
        val fakeRepo = repo as FakeDataSource
        user.onSignInResult(fakeRepo.user2id)
        user.userUniqueId.getOrAwaitValue()
        MatcherAssert.assertThat(user.userUniqueId.value, CoreMatchers.`is`(fakeRepo.user2id))

        // Verify correct reminders were loaded for user2
        vm.loadReminders()
        vm.remindersList.getOrAwaitValue()
        MatcherAssert.assertThat(vm.remindersList.value?.size, CoreMatchers.`is`(1))
        MatcherAssert.assertThat(
            vm.remindersList.value?.first()?.id,
            CoreMatchers.`is`(fakeRepo.reminder3.id)
        )
    }
}