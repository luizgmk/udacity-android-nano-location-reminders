package com.udacity.location_reminders.data.local

import com.udacity.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.data.dto.ReminderDTO
import com.udacity.location_reminders.data.dto.Result
import com.udacity.location_reminders.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.*

/**
 * Concrete implementation of a data source as a db.
 *
 * The repository is implemented so that you can focus on only testing it.
 *
 * @param remindersDao the dao that does the Room db operations
 * @param dispatcher a coroutine dispatcher to offload the blocking IO tasks
 */
class RemindersLocalRepository(
    private val remindersDao: RemindersDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ReminderDataSource {

    /**
     * Get the reminders list from the local db
     * @return Result the holds a Success with all the reminders or an Error object with the error message
     */
    override suspend fun getReminders(userUniqueId : String): Result<List<ReminderDTO>> = withContext(dispatcher) {
        return@withContext try {
            wrapEspressoIdlingResource {
                Result.Success(remindersDao.getReminders(userUniqueId))
            }
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    /**
     * Insert a reminder in the db.
     * @param reminder the reminder to be inserted
     */
    override suspend fun saveReminder(reminder: ReminderDTO) =
        withContext(dispatcher) {
            wrapEspressoIdlingResource {
                remindersDao.saveReminder(reminder)
            }
        }

    /**
     * Get a reminder by its id
     * @param id to be used to get the reminder
     * @return Result the holds a Success object with the Reminder or an Error object with the error message
     */
    override suspend fun getReminder(userUniqueId : String, id: String): Result<ReminderDTO> = withContext(dispatcher) {
        wrapEspressoIdlingResource {
            try {
                val reminder = remindersDao.getReminderById(id, userUniqueId)
                if (reminder != null) {
                    return@withContext Result.Success(reminder)
                } else {
                    return@withContext Result.Error("Reminder not found!")
                }
            } catch (e: Exception) {
                return@withContext Result.Error(e.localizedMessage)
            }
        }
    }

    /**
     * Deletes all the reminders in the db
     */
    override suspend fun deleteAllReminders(userUniqueId: String) {
        withContext(dispatcher) {
            wrapEspressoIdlingResource {
                remindersDao.deleteAllReminders(userUniqueId)
            }
        }
    }
}
