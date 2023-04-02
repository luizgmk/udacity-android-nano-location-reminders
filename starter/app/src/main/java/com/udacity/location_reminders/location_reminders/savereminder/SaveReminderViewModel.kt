package com.udacity.location_reminders.location_reminders.savereminder

import android.app.Application
import androidx.lifecycle.*
import com.udacity.location_reminders.R
import com.udacity.location_reminders.base.BaseViewModel
import com.udacity.location_reminders.base.NavigationCommand
import com.udacity.location_reminders.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.location_reminders.data.dto.ReminderDTO
import com.udacity.location_reminders.location_reminders.reminderslist.ReminderDataItem
import com.udacity.location_reminders.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    companion object {
        // Radius in meters
        const val DEFAULT_ROUND_GEOFENCE_RADIUS = 150
    }

    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData("")
    val reminderLatitude = MutableLiveData<Double?>()
    val reminderLongitude = MutableLiveData<Double?>()
    val reminderRoundGeofenceRadius = MutableLiveData(DEFAULT_ROUND_GEOFENCE_RADIUS)

    // Active selection
    val newSelectedLocationStr = MutableLiveData("")
    val newLatitude = MutableLiveData<Double?>()
    val newLongitude = MutableLiveData<Double?>()
    val newRoundGeofenceRadius = MutableLiveData(DEFAULT_ROUND_GEOFENCE_RADIUS)


    val locationSaved = SingleLiveEvent<Boolean>()

    fun saveLocation() {
        locationSaved.postValue(true)
    }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderLatitude.value = null
        reminderLongitude.value = null
        reminderRoundGeofenceRadius.value = DEFAULT_ROUND_GEOFENCE_RADIUS
        reminderSelectedLocationStr.value = ""

        newLatitude.value = null
        newLongitude.value = null
        newRoundGeofenceRadius.value = DEFAULT_ROUND_GEOFENCE_RADIUS
        newSelectedLocationStr.value = ""
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
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
}