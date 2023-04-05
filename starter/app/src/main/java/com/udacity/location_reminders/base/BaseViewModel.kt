package com.udacity.location_reminders.base

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseUser
import com.udacity.location_reminders.authentication.data.User
import com.udacity.location_reminders.authentication.data.UserPlugIn
import com.udacity.location_reminders.utils.SingleLiveEvent

/**
 * Base class for View Models to declare the common LiveData objects in one place
 */
abstract class BaseViewModel(app: Application) : AndroidViewModel(app) {

    var userUniqueId : String? = null
    open fun onLogoutCompleted() {
        userUniqueId = null
    }
    open fun onLoginSuccessful(user : FirebaseUser) {
        userUniqueId = user.uid
    }
    private val registration = object : UserPlugIn {
        override fun onLogout() {
            onLogoutCompleted()
        }
        override fun onLogin(user : FirebaseUser) {
            onLoginSuccessful(user)
        }
    }


    init {
        User.registerPlugin(registration)
    }

    override fun onCleared() {
        super.onCleared()
        User.unregisterPlugin(registration)
    }

    val navigationCommand: SingleLiveEvent<NavigationCommand> = SingleLiveEvent()
    val showErrorMessage: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBar: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()
    val showToast: SingleLiveEvent<String> = SingleLiveEvent()
    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val showNoData: MutableLiveData<Boolean> = MutableLiveData()

}