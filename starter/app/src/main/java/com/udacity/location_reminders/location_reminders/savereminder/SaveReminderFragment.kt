package com.udacity.location_reminders.location_reminders.savereminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.udacity.location_reminders.R
import com.udacity.location_reminders.base.BaseFragment
import com.udacity.location_reminders.base.NavigationCommand
import com.udacity.location_reminders.databinding.FragmentSaveReminderBinding
import com.udacity.location_reminders.utils.GeofencingHelper
import com.udacity.location_reminders.utils.Log
import com.udacity.location_reminders.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val vm: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private val geofencingHelper = GeofencingHelper(this)
    private val log = Log("SaveReminderFragment")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.vm = vm

        // When back from location selection, parameter is kept but view model is not null
        // and should not be overridden
        if (vm.reminder.value == null) {
            // Load reminder from navigation parameter if available
            val reminder = SaveReminderFragmentArgs.fromBundle(requireArguments()).reminder
            vm.editReminder(reminder)
        }

        // Prepare for Geofencing request
        GeofencingHelper.requestPermissions(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            vm.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val reminder = vm.reminder.value ?: return@setOnClickListener
            if (!vm.validateEnteredData(reminder)) return@setOnClickListener

            if (!GeofencingHelper.permissionsGranted) {
                // vm.showToast.value = getString(R.string.err_not_possible_to_save)
                vm.showToast.value = getString(R.string.permission_denied_explanation)
                return@setOnClickListener
            }
            // DONE: use the user entered reminder details to:
            // 1) add a geofencing request
            geofencingHelper.processRemindersGeofences(listOf(reminder),
                onSuccess = {
                    // DONE 2) save the reminder to the local db
                    vm.saveReminder(reminder)
                },
                onFailure = {
                    vm.showToast.value = it
                    log.e("Error trying to save a reminder \"${it}\"")
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        vm.onClear()
    }
}
