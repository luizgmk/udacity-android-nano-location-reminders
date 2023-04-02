package com.udacity.location_reminders.location_reminders.savereminder.selectreminderlocation


import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.distinctUntilChanged
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.location_reminders.R
import com.udacity.location_reminders.base.BaseFragment
import com.udacity.location_reminders.base.NavigationCommand
import com.udacity.location_reminders.databinding.FragmentSelectLocationBinding
import com.udacity.location_reminders.location_reminders.savereminder.SaveReminderViewModel
import com.udacity.location_reminders.utils.GoogleMapHelper
import com.udacity.location_reminders.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val vm: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var mapHelper: GoogleMapHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = vm
        binding.lifecycleOwner = this

        setupMenu()
        setDisplayHomeAsUpEnabled(true)

        // DONE: add the map setup implementation
        // DONE: zoom to the user location after taking his permission
        mapHelper = GoogleMapHelper(this).apply {
            requestLocationPermissions()
            onMapCallBack = {
                // DONE: put a marker to location that the user selected
                setMapClickListener(this.getMap())
                setPoiClickListener(this.getMap())

                // Restore to map the current geofence marker
                if (vm.reminderLongitude.value != null
                    && vm.reminderLatitude.value != null
                ) {
                    addMarker(
                        LatLng(vm.reminderLatitude.value!!, vm.reminderLongitude.value!!),
                        vm.reminderSelectedLocationStr.value
                    )
                    vm.newRoundGeofenceRadius.postValue(vm.reminderRoundGeofenceRadius.value)
                }
            }
        }
        setObservers()

        return binding.root
    }

    private fun setObservers() {
        mapHelper.currentPlaceName.distinctUntilChanged().observe(viewLifecycleOwner) {
            vm.newSelectedLocationStr.postValue(it)
        }
        vm.newRoundGeofenceRadius.observe(viewLifecycleOwner) {
            mapHelper.updateGeofenceRadius(it.toDouble())
        }
        vm.locationSaved.observe(viewLifecycleOwner) {
            // DONE: call this function after the user confirms on the selected location
            onLocationSelected()
        }
    }

    private fun onLocationSelected() {
        //        DONE: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence


        if (vm.newLatitude.value != null && vm.newLongitude.value != null) {
            vm.reminderLatitude.postValue(vm.newLatitude.value)
            vm.reminderLongitude.postValue(vm.newLongitude.value)
            vm.reminderSelectedLocationStr.postValue(vm.newSelectedLocationStr.value)
            vm.reminderRoundGeofenceRadius.postValue(vm.newRoundGeofenceRadius.value)
        }

        vm.navigationCommand.value = NavigationCommand.Back
    }

    private fun setupMenu() {
        // DONE: Change the map type based on the user's selection.
        val menuHost = (requireActivity() as MenuHost)
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.map_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    R.id.normal_map -> {
                        mapHelper.mapTypeNormal()
                    }
                    R.id.hybrid_map -> {
                        mapHelper.mapTypeHybrid()
                    }
                    R.id.satellite_map -> {
                        mapHelper.mapTypeStatellite()
                    }
                    R.id.terrain_map -> {
                        mapHelper.mapTypeTerrain()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setPoiClickListener(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            addMarker(poi.latLng, poi.name)
        }
    }

    private fun setMapClickListener(map: GoogleMap) = map.apply {
        this.setOnMapClickListener { latLng ->
            addMarker(latLng)
        }
    }

    private fun addMarker(position: LatLng, locationName: String? = null) {
        vm.newLatitude.postValue(position.latitude)
        vm.newLongitude.postValue(position.longitude)

        val marker = mapHelper.addMarker(
            name = locationName,
            options = MarkerOptions()
                .position(position)
                .title(getString(R.string.reminder_location))
                .snippet(
                    getString(
                        R.string.reminder_location_description,
                        position.latitude,
                        position.longitude
                    )
                )
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)),
            temporary = true,
            roundGeofenceRadius = vm.newRoundGeofenceRadius.value?.toDouble() ?: 100.0
        )
        mapHelper.moveCameraToLocation(position)
        marker?.showInfoWindow()
    }

}
