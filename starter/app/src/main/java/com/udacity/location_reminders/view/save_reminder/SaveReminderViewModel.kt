package com.udacity.location_reminders.view.save_reminder

import android.app.Application
import androidx.lifecycle.*
import com.udacity.location_reminders.R
import com.udacity.location_reminders.view.base.BaseViewModel
import com.udacity.location_reminders.view.base.NavigationCommand
import com.udacity.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.view.reminders_list.ReminderDataItem
import com.udacity.location_reminders.utils.Constants
import com.udacity.location_reminders.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class SaveReminderViewModel(
    private val app: Application,
    user: UserInterface,
    private val dataSource: ReminderDataSource
) :
    BaseViewModel(app, user) {

    val reminder = MutableLiveData<ReminderDataItem?>()
    val locationReminder = MutableLiveData<ReminderDataItem?>()

    val roundGeofenceRadiusSelection = MutableLiveData(Constants.DEFAULT_ROUND_GEOFENCE_RADIUS)

    val locationSaved = SingleLiveEvent<Boolean>()

    fun editReminder(reminder: ReminderDataItem) {
        this.reminder.postValue(reminder.copy())
        this.locationReminder.postValue(reminder.copy())
        this.roundGeofenceRadiusSelection.postValue(reminder.radius)
    }

    val geofenceLocationDescription: LiveData<String>
        get() = reminder.map {
            if (it?.location.isNullOrEmpty())
                app.getString(R.string.no_location_selected)
            else
                app.getString(
                    R.string.geofence_location_description,
                    it?.radius.toString(),
                    it?.location
                )
        }

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
        locationReminder.postValue(ReminderDataItem.getNewEmptyReminder(userUniqueId ?: ""))
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder() {
        val reminderData = reminder.value ?: return

        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(reminderData.toReminderDTO())
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(): Boolean {
        val reminderData = reminder.value ?: return false

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

    override fun onLoginSuccessful(uid: String) {
        onClear()
    }

    override fun onLogoutCompleted() {
        onClear()
    }
}