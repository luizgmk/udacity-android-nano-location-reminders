package com.udacity.location_reminders.view.reminders_list

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import com.udacity.location_reminders.data.dto.ReminderDTO
import com.udacity.location_reminders.utils.Constants
import kotlinx.coroutines.sync.Mutex
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
@Parcelize
data class ReminderDataItem(
    var userUniqueId: String?,
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
    var radius: Int?,
    val id: String = generateRandomUUID()
) : Parcelable {
    companion object {
        private var fixedUUID: String? = null
        private var mutex = Mutex()

        // during testing it's hard to mock verify if id keeps randomizing
        @VisibleForTesting
        fun fixUUID(value: String?) {
            synchronized(mutex) {
                fixedUUID = value
            }
        }

        fun generateRandomUUID(): String {
            return fixedUUID ?: UUID.randomUUID().toString()
        }

        fun getNewEmptyReminder(userUniqueId: String) = ReminderDataItem(
            userUniqueId = userUniqueId,
            title = null,
            description = null,
            location = null,
            latitude = null,
            longitude = null,
            radius = Constants.DEFAULT_ROUND_GEOFENCE_RADIUS
        )

        fun fromReminderDTO(reminder: ReminderDTO): ReminderDataItem = ReminderDataItem(
            reminder.userUniqueId,
            reminder.title,
            reminder.description,
            reminder.location,
            reminder.latitude,
            reminder.longitude,
            reminder.radius,
            reminder.id
        )
    }

    fun toReminderDTO(): ReminderDTO = ReminderDTO(
        userUniqueId!!,
        title,
        description,
        location,
        latitude,
        longitude,
        radius,
        id
    )

    fun getGlobalyUniqueId() = "${userUniqueId}/${id}"
}