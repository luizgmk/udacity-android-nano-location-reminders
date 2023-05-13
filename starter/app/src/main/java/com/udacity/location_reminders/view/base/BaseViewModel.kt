package com.udacity.location_reminders.view.base

import android.app.Application
import androidx.lifecycle.*
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.utils.SingleLiveEvent

/**
 * Base class for View Models to declare the common LiveData objects in one place
 */
abstract class BaseViewModel(app: Application, val user: UserInterface) : AndroidViewModel(app) {

    val authenticated = user.userUniqueId.map {
        if (it == null) onLogoutCompleted()
        else onLoginSuccessful(it)
        it != null
    }

    val userUniqueId
        get() = user.userUniqueId.value

    // for the top viewModel to override if something to do
    open fun onLogoutCompleted() {}

    // for the top viewModel to override if something to do
    open fun onLoginSuccessful(uid : String) {}

    val navigationCommand: SingleLiveEvent<NavigationCommand> = SingleLiveEvent()
    val showErrorMessage: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBar: SingleLiveEvent<String> = SingleLiveEvent()
    val showSnackBarInt: SingleLiveEvent<Int> = SingleLiveEvent()
    val showToast: SingleLiveEvent<String> = SingleLiveEvent()
    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val showNoData: MutableLiveData<Boolean> = MutableLiveData()

}