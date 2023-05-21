package com.udacity.location_reminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.location_reminders.data.FakeDataSource
import com.udacity.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.data.dto.Result
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.domain.UserTest
import com.udacity.location_reminders.test_utils.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest : KoinTest {

    private lateinit var database: RemindersDatabase
    private lateinit var fakeDataSource: FakeDataSource
    private val user: UserInterface by inject()
    private val repository: RemindersLocalRepository by inject()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        stopKoin()

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        GlobalContext.startKoin {
            modules(module {
                single<ReminderDataSource> { FakeDataSource() }
                single<UserInterface> { UserTest() }
                single { database.reminderDao() }
                single { RemindersLocalRepository(get(), mainCoroutineRule.dispatcher) }
            })
        }
        fakeDataSource = FakeDataSource()
        user.onSignInResult(fakeDataSource.user1id)
    }

    @After
    fun closeDB() = database.close()

    //    DONE: Add testing implementation to the RemindersLocalRepository.kt
    @Test
    fun getReminders() = runTest {
        // GIVEN the database contains 3 reminders, 2 for user 1, 1 for user 2
        database.reminderDao().saveReminder(fakeDataSource.reminder1)
        database.reminderDao().saveReminder(fakeDataSource.reminder2)
        database.reminderDao().saveReminder(fakeDataSource.reminder3)

        // WHEN retrieving reminders for each user
        val resultUser1 = repository.getReminders(fakeDataSource.user1id)
        val resultUser2 = repository.getReminders(fakeDataSource.user2id)

        // THEN result is successful
        MatcherAssert.assertThat(resultUser1 is Result.Success, CoreMatchers.`is`(true))
        MatcherAssert.assertThat(resultUser2 is Result.Success, CoreMatchers.`is`(true))

        // AND reminders match accordingly
        resultUser1 as Result.Success
        resultUser2 as Result.Success
        MatcherAssert.assertThat(resultUser1.data.size, CoreMatchers.`is`(2))
        MatcherAssert.assertThat(resultUser2.data.size, CoreMatchers.`is`(1))
        MatcherAssert.assertThat(
            resultUser1.data.first(),
            CoreMatchers.`is`(fakeDataSource.reminder1)
        )
        MatcherAssert.assertThat(
            resultUser1.data.last(),
            CoreMatchers.`is`(fakeDataSource.reminder2)
        )
        MatcherAssert.assertThat(
            resultUser2.data.first(),
            CoreMatchers.`is`(fakeDataSource.reminder3)
        )
    }

    @Test
    fun saveReminder() = runTest {
        // GIVEN the database is empty
        MatcherAssert.assertThat(
            database.reminderDao().countRemindersAllUsers(),
            CoreMatchers.`is`(0)
        )

        // WHEN requesting saving a reminder
        val reminder = fakeDataSource.reminder1
        repository.saveReminder(reminder)

        // THEN it is properly stored in the database
        val recoveredReminder =
            database.reminderDao().getReminderById(reminder.id, reminder.userUniqueId)
        MatcherAssert.assertThat(recoveredReminder, CoreMatchers.`is`(reminder))
    }

    @Test
    fun getReminder() = runTest {
        // GIVEN the database contains 3 reminders, 2 for user 1, 1 for user 2
        val r1 = fakeDataSource.reminder1
        val r2 = fakeDataSource.reminder2
        val r3 = fakeDataSource.reminder3
        database.reminderDao().saveReminder(r1)
        database.reminderDao().saveReminder(r2)
        database.reminderDao().saveReminder(r3)

        // WHEN retrieving each specific reminders
        val retrievedR1 = repository.getReminder(r1.userUniqueId, r1.id)
        val retrievedR2 = repository.getReminder(r2.userUniqueId, r2.id)
        val retrievedR3 = repository.getReminder(r3.userUniqueId, r3.id)

        // THEN result is successful
        MatcherAssert.assertThat(retrievedR1 is Result.Success, CoreMatchers.`is`(true))
        MatcherAssert.assertThat(retrievedR2 is Result.Success, CoreMatchers.`is`(true))
        MatcherAssert.assertThat(retrievedR3 is Result.Success, CoreMatchers.`is`(true))

        // AND reminders match accordingly
        retrievedR1 as Result.Success
        retrievedR2 as Result.Success
        retrievedR3 as Result.Success

        MatcherAssert.assertThat(retrievedR1.data, CoreMatchers.`is`(r1))
        MatcherAssert.assertThat(retrievedR2.data, CoreMatchers.`is`(r2))
        MatcherAssert.assertThat(retrievedR3.data, CoreMatchers.`is`(r3))
    }

    @Test
    fun getReminderWhenNonExistingOrWrongUser() = runTest {
        // GIVEN the database contains 3 reminders, 2 for user 1, 1 for user 2
        val r1 = fakeDataSource.reminder1
        val r2 = fakeDataSource.reminder2
        val r3 = fakeDataSource.reminder3
        database.reminderDao().saveReminder(r1)
        database.reminderDao().saveReminder(r2)
        database.reminderDao().saveReminder(r3)

        // WHEN trying to retrieve a reminder indicating the wrong user
        val retrievedR1 = repository.getReminder(r3.userUniqueId, r1.id)

        // THEN result is an error
        MatcherAssert.assertThat(retrievedR1 is Result.Error, CoreMatchers.`is`(true))

        // AND WHEN trying to retrieve a reminder that doesn't exist
        val retrievedR2 = repository.getReminder(r1.userUniqueId, "non-existent id")

        // THEN result is also an error
        MatcherAssert.assertThat(retrievedR2 is Result.Error, CoreMatchers.`is`(true))
    }

    @Test
    fun deleteAllReminders() = runTest {
        // GIVEN the database contains 3 reminders, 2 for user 1, 1 for user 2
        val r1 = fakeDataSource.reminder1
        val r2 = fakeDataSource.reminder2
        val r3 = fakeDataSource.reminder3
        database.reminderDao().saveReminder(r1)
        database.reminderDao().saveReminder(r2)
        database.reminderDao().saveReminder(r3)
        MatcherAssert.assertThat(
            database.reminderDao().countRemindersAllUsers(),
            CoreMatchers.`is`(3)
        )

        // WHEN deleting all reminders for user 1
        repository.deleteAllReminders(r1.userUniqueId)

        // THEN 1 reminder remains
        MatcherAssert.assertThat(
            database.reminderDao().countRemindersAllUsers(),
            CoreMatchers.`is`(1)
        )

        // AND WHEN deleting all reminders for user 2 after that
        repository.deleteAllReminders(r3.userUniqueId)

        // THEN no reminder remains
        MatcherAssert.assertThat(
            database.reminderDao().countRemindersAllUsers(),
            CoreMatchers.`is`(0)
        )

        // AND WHEN even if deleting for a non-existing user
        repository.deleteAllReminders("non-existent user id")

        // THEN still no reminder remains and no exception is raised
        MatcherAssert.assertThat(
            database.reminderDao().countRemindersAllUsers(),
            CoreMatchers.`is`(0)
        )
    }

}