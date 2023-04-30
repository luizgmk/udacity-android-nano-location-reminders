package com.udacity.location_reminders.view.reminders_list

import android.os.Parcelable
import com.udacity.location_reminders.utils.Constants
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
    val id: String = UUID.randomUUID().toString()
) : Parcelable {
    companion object {
        fun getNewEmptyReminder(userUniqueId: String) = ReminderDataItem(
            userUniqueId = userUniqueId,
            title = null,
            description = null,
            location = null,
            latitude = null,
            longitude = null,
            radius = Constants.DEFAULT_ROUND_GEOFENCE_RADIUS
        )
    }
    fun getGlobalyUniqueId() = "${userUniqueId}/${id}"
}