package com.udacity.location_reminders.domain

import android.app.Activity
import androidx.lifecycle.LiveData

interface UserInterface {
    val userUniqueId: LiveData<String?>
    val isAuthenticated: Boolean

    fun login(owner: Activity)
    fun logout()

    fun onSignInResult(newUserUniqueId : String?)
}