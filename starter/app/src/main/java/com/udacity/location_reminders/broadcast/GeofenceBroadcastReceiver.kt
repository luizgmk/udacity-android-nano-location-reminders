package com.udacity.location_reminders.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.location_reminders.authentication.data.User
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.utils.GeofencingHelper
import com.udacity.location_reminders.utils.sendNotification
import com.udacity.location_reminders.view.reminders_list.ReminderDataItem
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GeofenceBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val log = com.udacity.location_reminders.utils.Log("GeofenceBroadcastReceiver")
    private val user : UserInterface by inject()

    init {
        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
    }

    // DONE: implement the onReceive method to receive the geofencing events
    override fun onReceive(context: Context, intent: Intent) {
        log.i("Intent received from component/class ${intent.component?.className}")

        val reminder: ReminderDataItem?
        try {
            // User getParcelable due to replacement won't work with Android Q
            @Suppress("DEPRECATION")
            reminder = intent.extras?.getParcelable(GeofencingHelper.INTENT_EXTRA_REMINDER_KEY)
            if (reminder == null) {
                log.e("reminder's key properties were not available")
                return
            } else {
                log.i("Recovered reminder of guid ${reminder.getGlobalyUniqueId()} - ${reminder.title}")
            }
        } catch (e: Exception) {
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

        //DONE: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        //DONE: call @sendNotification


        if (reminder.userUniqueId == user.userUniqueId.value) {
            log.i("Notifying reminder title \"${reminder.title}\"")
            sendNotification(context, reminder)
        } else log.w(
            "Skipping geofence notification of title " +
                    "\"${reminder.title}\" due to a different user is logged in."
        )
    }
}