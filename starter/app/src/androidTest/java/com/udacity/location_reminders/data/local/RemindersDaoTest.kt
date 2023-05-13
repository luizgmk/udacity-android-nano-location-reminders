package com.udacity.location_reminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.location_reminders.data.dto.ReminderDTO
import com.udacity.location_reminders.data.local.LocalDB
import com.udacity.location_reminders.data.local.RemindersDatabase

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // DONE: Add testing implementation to the RemindersDao.kt
    private lateinit var database: RemindersDatabase

    private val user1id = "userUniqueId#1"
    private val user2id = "userUniqueId#2"

    private val reminder1 = ReminderDTO(
        user1id,
        "Reminder01",
        "description r1",
        "Some place 01",
        1.2312,
        1.12312,
        100,
        "reminder01"
    )

    private val reminder2 = ReminderDTO(
        user1id,
        "Reminder02",
        "description r2",
        "Some place 02",
        2.2312,
        2.12312,
        200,
        "reminder02"
    )

    private val reminder3 = ReminderDTO(
        user2id,
        "Reminder03",
        "description r3",
        "Some place 03",
        3.2312,
        3.12312,
        300,
        "reminder03"
    )


    @Before
    fun setupDB() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDB() = database.close()

    @Test
    fun getReminders() = runTest {
        val dao = database.reminderDao()
        dao.saveReminder(reminder1)
        dao.saveReminder(reminder2)
        dao.saveReminder(reminder3)

        val loadRemindersUser1 = dao.getReminders(user1id)
        val loadRemindersUser2 = dao.getReminders(user2id)

        // confirm counts
        MatcherAssert.assertThat(loadRemindersUser1.size, CoreMatchers.`is`(2))
        MatcherAssert.assertThat(loadRemindersUser2.size, CoreMatchers.`is`(1))

        // guarantee only target user reminders are loaded
        loadRemindersUser1.forEach { reminder ->
            MatcherAssert.assertThat(reminder.userUniqueId, CoreMatchers.`is`(user1id))
        }
        loadRemindersUser2.forEach { reminder ->
            MatcherAssert.assertThat(reminder.userUniqueId, CoreMatchers.`is`(user2id))
        }
    }

    // already tested by saveReminder
    //    @Test
    //    fun getReminderById() = runTest {
    //    }

    @Test
    fun saveReminder() = runTest {
        val dao = database.reminderDao()
        dao.saveReminder(reminder1)
        val loadedReminder = dao.getReminderById(reminder1.id, reminder1.userUniqueId)
        MatcherAssert.assertThat(
            loadedReminder,
            CoreMatchers.`is`(CoreMatchers.not(CoreMatchers.nullValue()))
        )
        MatcherAssert.assertThat(reminder1.id, CoreMatchers.`is`(loadedReminder?.id))
        MatcherAssert.assertThat(reminder1.title, CoreMatchers.`is`(loadedReminder?.title))
        MatcherAssert.assertThat(
            reminder1.description,
            CoreMatchers.`is`(loadedReminder?.description)
        )
        MatcherAssert.assertThat(
            reminder1.userUniqueId,
            CoreMatchers.`is`(loadedReminder?.userUniqueId)
        )
    }

    @Test
    fun deleteAllReminders() = runTest {
        val dao = database.reminderDao()
        dao.saveReminder(reminder1)
        dao.saveReminder(reminder2)
        dao.saveReminder(reminder3)

        var count = dao.countRemindersAllUsers()
        MatcherAssert.assertThat(count, CoreMatchers.`is`(3))

        dao.deleteAllReminders(user1id)
        dao.deleteAllReminders(user2id)

        count = dao.countRemindersAllUsers()
        MatcherAssert.assertThat(count, CoreMatchers.`is`(0))
    }


}