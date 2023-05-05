package com.udacity.location_reminders.view.reminders_list

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.udacity.location_reminders.view.base.BaseViewModel
import com.udacity.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.data.dto.ReminderDTO
import com.udacity.location_reminders.data.dto.Result
import kotlinx.coroutines.launch

class RemindersListViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        val uid = userUniqueId
        if (uid == null) {
            remindersList.value = listOf()
            invalidateShowNoData()
            return
        }
        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders(uid)
            showLoading.postValue(false)
            when (result) {
                is Result.Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    if (result.data is List<*>) {
                        dataList.addAll(result.data.mapNotNull { reminder ->
                            if (reminder is ReminderDTO) {
                                //map the reminder data from the DB to the be ready to be displayed on the UI
                                ReminderDataItem(
                                    reminder.userUniqueId,
                                    reminder.title,
                                    reminder.description,
                                    reminder.location,
                                    reminder.latitude,
                                    reminder.longitude,
                                    reminder.radius,
                                    reminder.id
                                )
                            } else null
                        })
                    }
                    remindersList.value = dataList
                }
                is Result.Error ->
                    showSnackBar.value = result.message
            }

            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }

    override fun onLoginSuccessful(user : FirebaseUser) {
        super.onLoginSuccessful(user)
        loadReminders()
    }

    override fun onLogoutCompleted() {
        super.onLogoutCompleted()
        loadReminders()
    }
}