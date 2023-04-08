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

        binding.vm = vm
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
                val longitude = vm.locationReminder.value?.longitude
                val latitude = vm.locationReminder.value?.latitude
                val location = vm.locationReminder.value?.location
                if (longitude != null && latitude != null) {
                    addMarker(
                        LatLng(latitude, longitude),
                        location
                    )
                }
            }
        }
        setObservers()

        return binding.root
    }

    private fun setObservers() {
        mapHelper.currentPlaceName.distinctUntilChanged().observe(viewLifecycleOwner) {
            val locationReminder = vm.locationReminder.value
            locationReminder?.location = it
            vm.locationReminder.postValue(locationReminder)
        }
        vm.roundGeofenceRadiusSelection.observe(viewLifecycleOwner) {
            mapHelper.updateGeofenceRadius(it)
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

        val latitude = vm.locationReminder.value?.latitude
        val longitude = vm.locationReminder.value?.longitude
        val location = vm.locationReminder.value?.location
        val radius = vm.roundGeofenceRadiusSelection.value
        if (latitude!= null && longitude!= null) {
            val reminder = vm.reminder.value!!

            reminder.longitude = longitude
            reminder.latitude = latitude
            reminder.location = location
            reminder.radius = radius

            vm.reminder.postValue(reminder)
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
        val reminder = vm.locationReminder.value ?: return
        reminder.location = locationName
        reminder.longitude = position.longitude // offline or GPS
        reminder.latitude = position.latitude // offline or GPS
        vm.locationReminder.postValue(reminder)

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
            roundGeofenceRadius = vm.locationReminder.value?.radius?.toDouble() ?: 100.0
        )
        mapHelper.moveCameraToLocation(position)
        marker?.showInfoWindow()
    }

}
