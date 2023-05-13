package com.udacity.location_reminders.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.location_reminders.R
import com.udacity.location_reminders.authentication.data.User
import com.udacity.location_reminders.databinding.ActivityReminderDescriptionBinding
import com.udacity.location_reminders.domain.UserInterface
import com.udacity.location_reminders.utils.Log
import com.udacity.location_reminders.view.reminders_list.ReminderDataItem
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity(), KoinComponent {

    companion object {
        private const val EXTRA_REMINDER_DATA_ITEM_KEY = "EXTRA_ReminderDataItem"
        private val log = Log("ReminderDescriptionActivity")

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_REMINDER_DATA_ITEM_KEY, reminderDataItem)
            return intent
        }
    }

    // This bypass the regular flow through a viewModel, but being the only data required
    // kept direct access to domain layer for simplicity
    private val user : UserInterface by inject()
    private lateinit var binding: ActivityReminderDescriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )

        // DONE: Add the implementation of the reminder details
        val reminder : ReminderDataItem
        try {
            // User getParcelable due to replacement won't work with Android Q
            @Suppress("DEPRECATION")
            reminder = intent.extras!!.getParcelable(EXTRA_REMINDER_DATA_ITEM_KEY)!!
            log.i("Recovered reminder data from notification of title \"${reminder.title}\"")
        } catch (e: Exception) {
            log.e("Error parsing or obtaining reminder data")
            return
        }
        if (reminder.userUniqueId != user.userUniqueId.value) {
            log.w("Reminder notification is not from the current user. Aborting.")
            finish()
        } else {
            // Display reminder details
            binding.reminderDataItem = reminder
        }
    }
}
