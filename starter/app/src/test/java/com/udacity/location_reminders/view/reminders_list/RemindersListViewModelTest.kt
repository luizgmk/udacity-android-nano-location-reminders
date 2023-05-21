package com.udacity.location_reminders.view.reminders_list

import android.app.Activity
import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.location_reminders.data.FakeDataSource
import com.udacity.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.domain.FakeUser
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
                    single<UserInterface> { FakeUser() }
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
    fun invalidateShowNoData() = runTest {
        // WHEN reminders are refreshed and no reminders are found
        vm.loadReminders()

        // THEN, invalidateShowNoData is called and showNoData value is set to true
        vm.showNoData.getOrAwaitValue()
        MatcherAssert.assertThat(vm.showNoData.value, CoreMatchers.`is`(true))

        // WHEN a user that has reminders registered logs in and reminders are refreshed
        val fakeRepo = repo as FakeDataSource
        user.onSignInResult(fakeRepo.user1id)
        vm.loadReminders()

        // THEN, invalidateShowNoData is called and showNoData value is set to false
        vm.showNoData.getOrAwaitValue()
        MatcherAssert.assertThat(vm.showNoData.value, CoreMatchers.`is`(false))

        // WHEN user logs out and reminders are emptied
        user.logout()
        vm.loadReminders()

        // THEN, invalidateShowNoData is called and showNoData value is set to true
        vm.showNoData.getOrAwaitValue()
        MatcherAssert.assertThat(vm.showNoData.value, CoreMatchers.`is`(true))
    }

    @Test
    fun onLoginSuccessAndLogoutCompleted() = runTest {
        // GIVEN no reminders are loaded when no user is logged in
        MatcherAssert.assertThat(vm.remindersList.value?.size, CoreMatchers.`is`(0))

        // WHEN a users logs in
        val fakeRepo = repo as FakeDataSource
        user.onSignInResult(fakeRepo.user1id)
        user.userUniqueId.getOrAwaitValue()
        vm.authenticated.getOrAwaitValue()
        vm.remindersList.getOrAwaitValue()

        // THEN reminders are loaded automatically for that user
        MatcherAssert.assertThat(vm.remindersList.value?.size, CoreMatchers.`is`(2))

        // BUT WHEN the user logs out
        user.logout()

        // THEN the reminderList is refreshed automatically, returning to an empty list
        user.userUniqueId.getOrAwaitValue()
        vm.authenticated.getOrAwaitValue()
        vm.remindersList.getOrAwaitValue()
        MatcherAssert.assertThat(vm.remindersList.value?.size, CoreMatchers.`is`(0))
    }

    @Test
    fun checkUserAuthenticationWhenUserIsLoggedInTriggersLogin() = runTest {
        // GIVEN no user is logged in
        // WHEN checking whether user is authenticated
        vm.checkUserAuthentication(Activity())
        val userTest = user as FakeUser
        userTest.loginTriggered.getOrAwaitValue()
        // THEN login is triggered
        MatcherAssert.assertThat(userTest.loginTriggered.value, CoreMatchers.`is`(true))
    }

    @Test
    fun checkUserAuthenticationWhenUserIsLoggedInDoNotTriggerLogin() = runTest {
        // GIVEN a user is logged in
        val fakeRepo = repo as FakeDataSource
        user.onSignInResult(fakeRepo.user1id)
        user.userUniqueId.getOrAwaitValue()

        // WHEN checking whether user is authenticated
        vm.checkUserAuthentication(Activity())
        val userTest = user as FakeUser
        userTest.loginTriggered.getOrAwaitValue()

        // THEN login is not triggered
        MatcherAssert.assertThat(userTest.loginTriggered.value, CoreMatchers.`is`(false))
    }

    @Test
    fun userLogoutWhenCalledTriggersLogin() = runTest {
        // GIVEN a user is logged in
        val fakeRepo = repo as FakeDataSource
        user.onSignInResult(fakeRepo.user1id)
        user.userUniqueId.getOrAwaitValue()

        // WHEN logout is triggered
        vm.userLogout(Activity())
        val userTest = user as FakeUser
        userTest.loginTriggered.getOrAwaitValue()

        // THEN login is automatically triggered
        MatcherAssert.assertThat(userTest.loginTriggered.value, CoreMatchers.`is`(true))
    }

}