package com.udacity.location_reminders.data.local

import com.udacity.location_reminders.data.FakeDataSource
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
 * @param dataSource the dao that does the Room db operations
 * @param dispatcher a coroutine dispatcher to offload the blocking IO tasks
 */
class FakeRemindersLocalRepository(
    private val dataSource: FakeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ReminderDataSource {

    companion object {
        private const val IO_DELAY = 200L
        const val MSG_DB_ERROR = "Some db error"
    }

    var simulateDatabaseError = false

    private suspend fun emulateIoConditions() {
        delay(IO_DELAY)
        if (simulateDatabaseError) throw Exception(MSG_DB_ERROR)
    }

    /**
     * Get the reminders list from the local db
     * @return Result the holds a Success with all the reminders or an Error object with the error message
     */
    override suspend fun getReminders(userUniqueId: String): Result<List<ReminderDTO>> =
        withContext(dispatcher) {
            return@withContext try {
                wrapEspressoIdlingResource {
                    emulateIoConditions()
                    dataSource.getReminders(userUniqueId)
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
                emulateIoConditions()
                dataSource.saveReminder(reminder)
            }
        }

    /**
     * Get a reminder by its id
     * @param id to be used to get the reminder
     * @return Result the holds a Success object with the Reminder or an Error object with the error message
     */
    override suspend fun getReminder(userUniqueId: String, id: String): Result<ReminderDTO> =
        withContext(dispatcher) {
            try {
                wrapEspressoIdlingResource {
                    emulateIoConditions()
                    return@withContext dataSource.getReminder(id, userUniqueId)
                }
            } catch (e: Exception) {
                return@withContext Result.Error(e.localizedMessage)
            }
        }

    /**
     * Deletes all the reminders in the db
     */
    override suspend fun deleteAllReminders(userUniqueId: String) {
        withContext(dispatcher) {
            wrapEspressoIdlingResource {
                emulateIoConditions()
                dataSource.deleteAllReminders(userUniqueId)
            }
        }
    }
}
