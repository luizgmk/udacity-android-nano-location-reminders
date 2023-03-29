package com.udacity.location_reminders.authentication

import android.app.Application
import com.udacity.location_reminders.base.BaseViewModel
import com.udacity.location_reminders.base.NavigationCommand
import com.udacity.location_reminders.utils.SingleLiveEvent

class AuthenticationViewModel(app: Application) : BaseViewModel(app) {

    val launchLoginUIEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()

    fun launchLoginUI() {
        launchLoginUIEvent.postValue(true)
    }

}