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
        // Unfortunately, couldn't figure out how to prevent Koin from being already started
        // So stopping Koin at start to prevent exception
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
        // GIVEN no user is logged in
        // WHEN loading reminders
        vm.loadReminders()
        vm.remindersList.getOrAwaitValue()

        // THEN an empty list of reminders is loaded
        MatcherAssert.assertThat(user.userUniqueId.value, CoreMatchers.`is`(nullValue()))
        MatcherAssert.assertThat(vm.remindersList.value?.size, CoreMatchers.`is`(0))
    }

    @Test
    fun loadRemindersWhenUser1LogsIn() = runTest {
        // GIVEN no reminders are loaded
        vm.loadReminders()
        vm.remindersList.getOrAwaitValue()
        MatcherAssert.assertThat(vm.remindersList.value?.size, CoreMatchers.`is`(0))

        // GIVEN user1 logs in
        val fakeRepo = repo as FakeDataSource
        user.onSignInResult(fakeRepo.user1id)
        user.userUniqueId.getOrAwaitValue()
        MatcherAssert.assertThat(user.userUniqueId.value, CoreMatchers.`is`(fakeRepo.user1id))

        // THEN, correct reminders are loaded for user1
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
    fun loadRemindersWhenUser2LogsIn() = runTest {
        // GIVEN no reminders are loaded
        vm.loadReminders()
        vm.remindersList.getOrAwaitValue()
        MatcherAssert.assertThat(vm.remindersList.value?.size, CoreMatchers.`is`(0))

        // GIVEN user2 logs in
        val fakeRepo = repo as FakeDataSource
        user.onSignInResult(fakeRepo.user2id)
        user.userUniqueId.getOrAwaitValue()
        MatcherAssert.assertThat(user.userUniqueId.value, CoreMatchers.`is`(fakeRepo.user2id))

        // THEN, correct reminders are loaded for user2
        vm.loadReminders()
        vm.remindersList.getOrAwaitValue()
        MatcherAssert.assertThat(vm.remindersList.value?.size, CoreMatchers.`is`(1))
        MatcherAssert.assertThat(
            vm.remindersList.value?.first()?.id,
            CoreMatchers.`is`(fakeRepo.reminder3.id)
        )
    }

    @Test
    fun invalidateShowNoDataWhenNoRemindersIsTrue() = runTest {
        TODO("Pending")
    }

    @Test
    fun invalidateShowNoDataWhenNoRemindersIsFalse() = runTest {
        TODO("Pending")
    }

    @Test
    fun onLoginSuccessfulWhenUserLogsInIsCalled() = runTest {
        TODO("Pending")
    }

    @Test
    fun onLogoutCompletedWhenUserLogsOutIsCalled() = runTest {
        TODO("Pending")
    }

    @Test
    fun checkUserAuthenticationWhenUserIsLoggedInTriggersLogin() = runTest {
        TODO("Pending")
    }

    @Test
    fun checkUserAuthenticationWhenUserIsLoggedInDoNotTriggerLogin() = runTest {
        TODO("Pending")
    }

    @Test
    fun userLogoutWhenCalledTriggersLogin() = runTest {
        TODO("Pending")
    }

}