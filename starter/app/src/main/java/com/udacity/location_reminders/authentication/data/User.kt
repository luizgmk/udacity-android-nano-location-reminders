package com.udacity.location_reminders.authentication.data

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.udacity.location_reminders.R
import com.udacity.location_reminders.authentication.AuthenticationActivity

object User {

    private val auth = Firebase.auth

    private val internalCurrentUser = MutableLiveData(auth.currentUser)
    val currentUser: LiveData<FirebaseUser?>
        get() = internalCurrentUser

    val isAuthenticated
        get() = currentUser.value != null

    fun login(owner: FragmentActivity) {
        logout()
        val authIntent = Intent(owner, AuthenticationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        owner.startActivity(authIntent)
    }

    private fun logout() {
        if (isAuthenticated) {
            auth.signOut()
            internalCurrentUser.postValue(null)
        }
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

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult?) {
        if (result?.resultCode == AppCompatActivity.RESULT_OK) {
            // Successfully signed in
            internalCurrentUser.postValue(User.auth.currentUser)
        } else {
            // Sign in failed. If response is null the user canceled the
            // Make sure to eliminate any previous user auth
            internalCurrentUser.postValue(null)
        }
    }

}