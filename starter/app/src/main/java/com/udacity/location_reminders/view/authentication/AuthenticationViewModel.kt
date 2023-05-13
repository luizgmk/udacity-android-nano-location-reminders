package com.udacity.location_reminders.view.authentication

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.udacity.location_reminders.R
import com.udacity.location_reminders.authentication.data.User
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.view.base.BaseViewModel
import com.udacity.location_reminders.utils.SingleLiveEvent
import com.udacity.location_reminders.view.RemindersActivity

class AuthenticationViewModel(app: Application, user: UserInterface) : BaseViewModel(app, user) {

    private val auth = Firebase.auth

    val launchLoginUIEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()

    fun launchLoginUI() {
        launchLoginUIEvent.postValue(true)
    }

    fun buildSignInIntent(): Intent {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // DONE: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
        val customLayout = AuthMethodPickerLayout
            .Builder(R.layout.fui_custom_auth_method_picker_layout)
            .setGoogleButtonId(R.id.googleSignInButton)
            .setEmailButtonId(R.id.emailSignInButton)
            .build()

        // Create and launch sign-in intent
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.AppTheme)
            .setLogo(R.drawable.map)
            .setAuthMethodPickerLayout(customLayout)
            .build()
    }

    fun isAuthenticated() = user.isAuthenticated

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult?) {
        if (result?.resultCode == AppCompatActivity.RESULT_OK && auth.currentUser != null)
            // Successfully signed in
            user.onSignInResult(auth.currentUser?.uid)
        // Sign in failed. If response is null the user canceled the
        // Make sure to eliminate any previous user auth
        else user.onSignInResult(null)
    }

}