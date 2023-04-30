package com.udacity.location_reminders.data

import com.udacity.location_reminders.data.dto.ReminderDTO
import com.udacity.location_reminders.data.dto.Result

/**
 * Main entry point for accessing reminders data.
 */
interface ReminderDataSource {
    suspend fun getReminders(userUniqueId : String): Result<List<ReminderDTO>>
    suspend fun saveReminder(reminder: ReminderDTO)
    suspend fun getReminder(userUniqueId : String, id: String): Result<ReminderDTO>
    suspend fun deleteAllReminders(userUniqueId : String)
}