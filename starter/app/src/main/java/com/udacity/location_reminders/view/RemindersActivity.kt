package com.udacity.location_reminders.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.udacity.location_reminders.R

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_reminders)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val view = item.actionView
        if (view != null) {
            when (item.itemId) {
                android.R.id.home -> {
                    Navigation.findNavController(view).popBackStack()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
