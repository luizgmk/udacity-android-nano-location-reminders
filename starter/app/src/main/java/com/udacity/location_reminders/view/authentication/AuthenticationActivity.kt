package com.udacity.location_reminders.view.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.udacity.location_reminders.R
import com.udacity.location_reminders.authentication.data.User
import com.udacity.location_reminders.databinding.ActivityAuthenticationBinding
import com.udacity.location_reminders.view.RemindersActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        vm.onSignInResult(result)
    }

    private lateinit var binding: ActivityAuthenticationBinding

    private val vm: AuthenticationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        binding.vm = vm

        setContentView(binding.root)

        // Login button click event
        vm.launchLoginUIEvent.observe(this) {
            // DONE: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
            signInLauncher.launch(vm.buildSignInIntent())
        }

        // Result from Auth activity
        vm.authenticated.observe(this) {
            if (vm.isAuthenticated()) {
                // DONE: If the user was authenticated, send him to RemindersActivity
                // Navigate if authentication was successful
                startActivity(
                    Intent(this, RemindersActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            }
        }
    }

}
