package com.udacity.location_reminders.location_reminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.location_reminders.location_reminders.reminderslist.ReminderDataItem
import com.udacity.location_reminders.utils.GeofencingHelper
import com.udacity.location_reminders.utils.sendNotification

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private val log = com.udacity.location_reminders.utils.Log("GeofenceBroadcastReceiver")

    // DONE: implement the onReceive method to receive the geofencing events
    override fun onReceive(context: Context, intent: Intent) {
        log.i("Intent received from component/class ${intent.component?.className}")

        val reminder: ReminderDataItem?
        try {
            reminder = intent.extras?.getParcelable(GeofencingHelper.INTENT_EXTRA_REMINDER_KEY)
            if (reminder == null) {
                log.e("reminder's key properties were not available")
                return
            }
        } catch (e : Exception) {
            log.e("error parsing reminder data (message: \"${e.message}\")")
            return
        }

        // Intents are not consistently returning as a GeofencingEvent from GMS. To address that,
        // we send the notification even if Intent is not a GeofenceEvent, as long as the reminder
        // information can be recovered. At this moment, the Geofence request registers only
        // ENTER transition, so checking it on geofence itself adds no value. Still, when a
        // GeofencingEvent comes properly, the code checks for right transition and for
        // Geofencing errors, avoiding the notification when issues are found
        GeofencingEvent.fromIntent(intent)?.let { geofencingEvent ->
            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.errorCode)
                log.e(errorMessage)
                return
            }

            // Get the transition type.
            val geofenceTransition = geofencingEvent.geofenceTransition
            log.i("Intent received :: transition is $geofenceTransition")

            // Ignore in case the transition is not of interest
            if (geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) {
                log.w("Unexpected geofencing transition notification $geofenceTransition")
                return
            }
        }

        // if not errors from the geofence or not a GeofenceEvent Intent (due to Android
        // inconsistent behavior), recover the reminder and send the app notification
        sendNotification(context, reminder)
    }
}