package com.udacity.location_reminders.authentication.data

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.view.authentication.AuthenticationActivity
import com.udacity.location_reminders.utils.Log

class User(private val auth : FirebaseAuth) : UserInterface {

    private val log = Log("UserDomain")

    private val internalUniqueUserId = MutableLiveData(auth.currentUser?.uid)
    override val userUniqueId: LiveData<String?>
        get() = internalUniqueUserId

    override val isAuthenticated
        get() = userUniqueId.value != null

    override fun login(owner: Activity) {
        log.i("Login requested")
        logout()
        val authIntent = Intent(owner, AuthenticationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        owner.startActivity(authIntent)
    }

    override fun logout() {
        if (isAuthenticated) {
            log.i("Signing out")
            auth.signOut()
            internalUniqueUserId.postValue(null)
        }
    }

    override fun onSignInResult(newUserUniqueId: String?) {
        internalUniqueUserId.postValue(newUserUniqueId)
    }

}