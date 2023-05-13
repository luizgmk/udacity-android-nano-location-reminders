package com.udacity.location_reminders.domain

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class UserTest : UserInterface {

    override fun onSignInResult(newUserUniqueId: String?) {
        internalUniqueUserId.postValue(newUserUniqueId)
    }

    private val internalUniqueUserId : MutableLiveData<String?> = MutableLiveData(null)

    override val userUniqueId: LiveData<String?>
        get() = internalUniqueUserId

    override val isAuthenticated
        get() = userUniqueId.value != null

    override fun login(owner: Activity) {
        TODO("Not yet implemented")
    }

    override fun logout() {
        internalUniqueUserId.postValue(null)
    }
}