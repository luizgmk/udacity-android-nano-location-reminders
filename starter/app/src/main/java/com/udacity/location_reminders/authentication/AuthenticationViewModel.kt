package com.udacity.location_reminders.authentication

import android.app.Application
import com.google.firebase.auth.FirebaseUser
import com.udacity.location_reminders.view.base.BaseViewModel
import com.udacity.location_reminders.utils.SingleLiveEvent

class AuthenticationViewModel(app: Application) : BaseViewModel(app) {

    val launchLoginUIEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()

    override fun onLogoutCompleted() {
        // do nothing
    }

    override fun onLoginSuccessful(user : FirebaseUser) {
        // do nothing
    }

    fun launchLoginUI() {
        launchLoginUIEvent.postValue(true)
    }

}