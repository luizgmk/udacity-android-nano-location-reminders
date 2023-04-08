package com.udacity.location_reminders.location_reminders.savereminder

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseUser
import com.udacity.location_reminders.R
import com.udacity.location_reminders.base.BaseViewModel
import com.udacity.location_reminders.base.NavigationCommand
import com.udacity.location_reminders.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.location_reminders.data.dto.ReminderDTO
import com.udacity.location_reminders.location_reminders.reminderslist.ReminderDataItem
import com.udacity.location_reminders.utils.Constants
import com.udacity.location_reminders.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    val reminder = MutableLiveData<ReminderDataItem?>()
    val locationReminder = MutableLiveData<ReminderDataItem?>()

    val roundGeofenceRadiusSelection = MutableLiveData(Constants.DEFAULT_ROUND_GEOFENCE_RADIUS)

    val locationSaved = SingleLiveEvent<Boolean>()

    fun editReminder(reminder: ReminderDataItem) {
        this.reminder.postValue(reminder.copy())
        this.locationReminder.postValue(reminder.copy())
        this.roundGeofenceRadiusSelection.postValue(reminder.radius)
    }

    val geofenceLocationDescription: String
        get() = if (reminder.value?.location.isNullOrEmpty())
            app.getString(R.string.no_location_selected)
        else
            app.getString(
                R.string.geofence_location_description,
                reminder.value?.radius.toString(),
                reminder.value?.location
            )

    fun saveLocation() {
        if (locationReminder.value?.location.isNullOrEmpty()) {
            showSnackBar.value = app.getString(R.string.select_location)
        } else {
            locationSaved.postValue(true)
        }
    }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminder.postValue(null)
        locationReminder.postValue(ReminderDataItem.getNewEmptyReminder(userUniqueId!!))
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder() {
        val reminderData = reminder.value ?: return
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
        }
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    userUniqueId!!,
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.radius,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.userUniqueId.isNullOrEmpty()
            || reminderData.userUniqueId != userUniqueId
        ) {
            // should never ever get here, but if something has been missed, shut this all down
            throw SecurityException("No user logged in or the user is not the reminder's owner")
        }

        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }

        if (reminderData.radius == null || reminderData.radius!! !in 1..300) {
            showSnackBarInt.value = R.string.err_invalid_radius
            return false
        }
        return true
    }

    override fun onLoginSuccessful(user: FirebaseUser) {
        onClear()
    }

    override fun onLogoutCompleted() {
        onClear()
    }
}