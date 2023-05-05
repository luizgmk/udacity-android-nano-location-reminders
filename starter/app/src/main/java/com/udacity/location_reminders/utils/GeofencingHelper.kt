package com.udacity.location_reminders.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.location_reminders.R
import com.udacity.location_reminders.broadcast.GeofenceBroadcastReceiver
import com.udacity.location_reminders.view.reminders_list.ReminderDataItem
import kotlinx.coroutines.sync.Mutex
import java.util.*

class GeofencingHelper(fragment: Fragment) {

    private val fragment: Fragment

    companion object {
        const val INTENT_EXTRA_REMINDER_KEY = "reminder"

        private val mutex = Mutex(false)
        private val log = Log("GeofencingHelper")
        var permissionsGranted = false
        fun requestPermissions(fragment: Fragment) {
            fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
                // if any permission was granted, attempt to enable my location as it will check
                // for all permissions anyways
                var fineLocationGranted = false
                var coarseLocationGranted = false
                var backgroundLocationGranted = false
                permissionsResult.forEach {
                    when (it.key) {
                        Manifest.permission.ACCESS_COARSE_LOCATION -> coarseLocationGranted =
                            it.value
                        Manifest.permission.ACCESS_FINE_LOCATION -> fineLocationGranted = it.value
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION -> backgroundLocationGranted =
                            it.value
                    }
                    log.i("Permission result for ${it.key} is ${it.value}")
                }
                synchronized(mutex) {
                    permissionsGranted = fineLocationGranted &&
                            coarseLocationGranted &&
                            backgroundLocationGranted
                }
            }.launch(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                else arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    init {
        this.fragment = fragment
    }

    @SuppressLint("MissingPermission")
    fun processRemindersGeofences(
        reminders: List<ReminderDataItem>,
        onSuccess: () -> Unit,
        onFailure: (failureMessage: String) -> Unit
    ) {
        if (!permissionsGranted || reminders.isEmpty()) return

        // Intent's are not consistently coming as a GeofenceEvent from GMS. To address this
        // in a consistent way, this code has turned to issue independent PendingIntents per
        // reminder allowing to recover the reminder information at least.
        reminders.forEach { reminder ->
            reminderToGeofence(reminder)?.let { geofence ->
                log.i("Removing geofence of id ${geofence.requestId}")
                geofencingClient.removeGeofences(mutableListOf(geofence.requestId)).run {
                    addOnSuccessListener {
                        // Geofences removed
                        geofencingClient.addGeofences(
                            buildGeofencingRequest(geofence),
                            buildGeofencePendingIntent(reminder)
                        ).run {
                            addOnSuccessListener {
                                // Geofences added
                                log.i("Adding geofence ID ${geofence.requestId} was successful")
                                onSuccess()
                            }
                            addOnFailureListener {
                                // Failed to add geofences
                                log.e("Adding geofence ID ${geofence.requestId} has failed")
                                onFailure(parseGeofenceException(it))
                            }
                        }
                    }
                    addOnFailureListener {
                        // Failed to remove geofences
                        log.e("Removing geofence ID ${geofence.requestId} has failed")
                        onFailure(parseGeofenceException(it))
                    }
                }
            }
        }
    }

    private fun parseGeofenceException(e: Exception): String {
        var statusCode = if (e is ApiException) e.statusCode else -1
        return when (statusCode) {
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> fragment.getString(R.string.geofence_too_many_geofences)
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> fragment.getString(R.string.geofence_not_available)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> fragment.getString(R.string.geofence_too_many_pending_intents)
            else -> fragment.getString(R.string.geofence_unknown_error)
        }
    }

    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(fragment.requireActivity())
    }

    private fun buildGeofencingRequest(geofence: Geofence) = GeofencingRequest.Builder().apply {
        setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        addGeofence(geofence)
    }.build()

    private fun buildGeofencePendingIntent(reminder: ReminderDataItem): PendingIntent {
        val activity = fragment.requireActivity()
        val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)

        log.i("Creating pending intent for reminder of guid ${reminder.getGlobalyUniqueId()}")
        intent.putExtra(INTENT_EXTRA_REMINDER_KEY, reminder)

        return PendingIntent.getBroadcast(
            activity.applicationContext,
            // make this unique
            "${reminder.getGlobalyUniqueId()}#geofencePendingIntent".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun reminderToGeofence(reminder : ReminderDataItem) : Geofence? {
        if (reminder.userUniqueId == null) return null
        val longitude = reminder.longitude ?: return null
        val latitude = reminder.latitude ?: return null
        val radius = reminder.radius?.toFloat() ?: return null

        return Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(reminder.getGlobalyUniqueId())
            // Set the circular region of this geofence.
            .setCircularRegion(latitude, longitude, radius)
            // Never expiring
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            // Create the geofence.
            .build()
    }

}