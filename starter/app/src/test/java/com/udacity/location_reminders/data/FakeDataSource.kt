package com.udacity.location_reminders.data

import com.udacity.location_reminders.data.dto.ReminderDTO
import com.udacity.location_reminders.data.dto.Result
import com.udacity.location_reminders.utils.SingleLiveEvent
import kotlinx.coroutines.*

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    // DONE: Create a fake data source to act as a double to the real data source
    companion object {
        private const val serviceLatencyInMillis = 500L
    }

    val user1id = "userUniqueId#1"
    val user2id = "userUniqueId#2"

    val reminder1 = ReminderDTO(
        user1id,
        "Reminder01",
        "description r1",
        "Some place 01",
        1.2312,
        1.12312,
        100,
        "reminder01"
    )

    val reminder2 = ReminderDTO(
        user1id,
        "Reminder02",
        "description r2",
        "Some place 02",
        2.2312,
        2.12312,
        200,
        "reminder02"
    )

    val reminder3 = ReminderDTO(
        user2id,
        "Reminder03",
        "description r3",
        "Some place 03",
        3.2312,
        3.12312,
        300,
        "reminder03"
    )

    private val reminders: LinkedHashMap<String, MutableList<ReminderDTO>> =
        linkedMapOf(
            user1id to mutableListOf(reminder1, reminder2),
            user2id to mutableListOf(reminder3)
        )

    override suspend fun getReminders(userUniqueId: String): Result<List<ReminderDTO>> {
        return Result.Success(reminders[userUniqueId] as List<ReminderDTO>)
    }

    val lastSavedReminder = SingleLiveEvent<ReminderDTO?>()
    override suspend fun saveReminder(reminder: ReminderDTO) {
        lastSavedReminder.postValue(reminder)
        delay(serviceLatencyInMillis)
        if (reminders[reminder.userUniqueId] == null) {
            reminders[reminder.userUniqueId] = mutableListOf()
        } else {
            reminders[reminder.userUniqueId]?.removeIf { it.id == reminder.id }
        }
        reminders[reminder.userUniqueId]?.add(reminder)
    }

    override suspend fun getReminder(userUniqueId: String, id: String): Result<ReminderDTO> {
        // DONE("return the reminder with the id")
        val reminder = reminders[userUniqueId]?.find { it.id == id }
        return if (reminder == null)
            Result.Error("Reminder not found!")
        else
            Result.Success(reminder)
    }

    override suspend fun deleteAllReminders(userUniqueId: String) {
        // DONE("delete all the reminders")
        reminders.remove(userUniqueId)
    }


}