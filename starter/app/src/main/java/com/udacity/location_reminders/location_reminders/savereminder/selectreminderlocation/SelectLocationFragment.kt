package com.udacity.location_reminders.location_reminders.savereminder.selectreminderlocation


import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.udacity.location_reminders.R
import com.udacity.location_reminders.base.BaseFragment
import com.udacity.location_reminders.databinding.FragmentSelectLocationBinding
import com.udacity.location_reminders.location_reminders.savereminder.SaveReminderViewModel
import com.udacity.location_reminders.utils.GoogleMapHelper
import com.udacity.location_reminders.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // DONE: add the map setup implementation
        // DONE: zoom to the user location after taking his permission
        GoogleMapHelper(this).requestLocationPermissions()

        // TODO: add style to the map
        // TODO: put a marker to location that the user selected
        // TODO: call this function after the user confirms on the selected location

        onLocationSelected()

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            true
        }
        R.id.hybrid_map -> {
            true
        }
        R.id.satellite_map -> {
            true
        }
        R.id.terrain_map -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


}