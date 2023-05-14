package com.udacity.location_reminders.view.save_reminder

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.udacity.location_reminders.R
import com.udacity.location_reminders.view.base.BaseFragment
import com.udacity.location_reminders.view.base.NavigationCommand
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

        setupMenu()

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

    // Sets up the options menu by using parent activity as a MenuHost
    // followed article for migration from deprecated onCreateOptionsMenu
    // https://medium.com/tech-takeaways/how-to-migrate-the-deprecated-oncreateoptionsmenu-b59635d9fe10
    private fun setupMenu() {
        setDisplayHomeAsUpEnabled(true)

        val menuHost = (requireActivity() as MenuHost)
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                return // no menu to create, only back button
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                log.i("${menuItem.itemId}")
                if (menuItem.itemId == android.R.id.home)
                    vm.navigationCommand.value = NavigationCommand.Back
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
            if (!vm.validateEnteredData()) return@setOnClickListener

            if (!GeofencingHelper.permissionsGranted) {
                // vm.showToast.value = getString(R.string.err_not_possible_to_save)
                vm.showToast.value = getString(R.string.permission_denied_explanation)
                return@setOnClickListener
            }
            // DONE: use the user entered reminder details to:
            // 1) add a geofencing request
            log.i("Requesting processing reminder of guid ${reminder.getGlobalyUniqueId()}")
            geofencingHelper.processRemindersGeofences(listOf(reminder),
                onSuccess = {
                    // DONE 2) save the reminder to the local db
                    vm.saveReminder()
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
