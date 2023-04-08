package com.udacity.location_reminders.location_reminders.reminderslist

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.udacity.location_reminders.R
import com.udacity.location_reminders.authentication.data.User
import com.udacity.location_reminders.base.BaseFragment
import com.udacity.location_reminders.base.NavigationCommand
import com.udacity.location_reminders.databinding.FragmentRemindersBinding
import com.udacity.location_reminders.utils.setDisplayHomeAsUpEnabled
import com.udacity.location_reminders.utils.setTitle
import com.udacity.location_reminders.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val vm: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = vm

        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))
        setupMenu()

        binding.refreshLayout.setOnRefreshListener { vm.loadReminders() }

        if (!User.isAuthenticated) User.login(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder(ReminderDataItem.getNewEmptyReminder(vm.userUniqueId!!))
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        vm.loadReminders()
    }

    private fun navigateToAddReminder(reminder : ReminderDataItem) {
        //use the navigationCommand live data to navigate between the fragments
        vm.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder(reminder)
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
            navigateToAddReminder(it)
        }

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    // Sets up the options menu by using parent activity as a MenuHost
    // followed article for migration from deprecated onCreateOptionsMenu
    // https://medium.com/tech-takeaways/how-to-migrate-the-deprecated-oncreateoptionsmenu-b59635d9fe10
    private fun setupMenu() {
        val menuHost = (requireActivity() as MenuHost)
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    R.id.logout -> {
                        User.login(requireActivity())
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

}
