package com.udacity.location_reminders.authentication

import android.app.Activity
import android.app.Application
import android.content.Intent
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseUser
import com.udacity.location_reminders.authentication.data.User
import com.udacity.location_reminders.view.base.BaseViewModel
import com.udacity.location_reminders.utils.SingleLiveEvent
import com.udacity.location_reminders.view.RemindersActivity

class AuthenticationViewModel(app: Application, user : User) : BaseViewModel(app, user) {

    val launchLoginUIEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val currentUser
        get() = user.currentUser

    override fun onLogoutCompleted() {
        // do nothing
    }

    override fun onLoginSuccessful(user : FirebaseUser) {
        // do nothing
    }

    fun launchLoginUI() {
        launchLoginUIEvent.postValue(true)
    }

    fun buildSignInIntent() = user.buildSignInIntent()

    fun isAuthenticated() = user.isAuthenticated

    fun onSignInResult(result : FirebaseAuthUIAuthenticationResult) {
        user.onSignInResult(result)
    }

}