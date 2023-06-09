package com.udacity.location_reminders.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.udacity.location_reminders.data.dto.ReminderDTO
import org.jetbrains.annotations.TestOnly

/**
 * Data Access Object for the reminders table.
 */
@Dao
interface RemindersDao {
    /**
     * @return all reminders.
     */
    @Query("SELECT * FROM reminders WHERE user_uid = :user_uid")
    suspend fun getReminders(user_uid: String): List<ReminderDTO>

    /**
     * @param reminderId the id of the reminder
     * @return the reminder object with the reminderId
     */
    @Query("SELECT * FROM reminders where entry_id = :reminderId and user_uid = :user_uid")
    suspend fun getReminderById(reminderId: String, user_uid: String): ReminderDTO?

    /**
     * Insert a reminder in the database. If the reminder already exists, replace it.
     *
     * @param reminder the reminder to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReminder(reminder: ReminderDTO)

    /**
     * Delete all reminders.
     */
    @Query("DELETE FROM reminders where user_uid = :user_uid")
    suspend fun deleteAllReminders(user_uid : String)

    /**
     * FOR TESTING
     * Count reminders in DB.
     */
    @TestOnly
    @Query("SELECT count(1) FROM reminders")
    suspend fun countRemindersAllUsers() : Int

}