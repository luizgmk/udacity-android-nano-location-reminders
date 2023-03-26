package com.udacity.location_reminders.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.udacity.location_reminders.R


class GoogleMapHelper(private val fragment: Fragment) : OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val activity: Activity
    private val mapFragment: SupportMapFragment
    private var permissionsGranted = false

    init {
        activity = fragment.requireActivity()
        mapFragment = fragment.childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        // Return from permissions request may call this before map is ready
        if (!this::map.isInitialized) return

        if (permissionsGranted) {
            map.isMyLocationEnabled = true
            moveCameraToCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        if (!permissionsGranted) return null
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }

    private fun moveCameraToCurrentLocation() {
        val location = getLastKnownLocation() ?: return

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(location.latitude, location.longitude), 100f
        )
        map.animateCamera(cameraUpdate)
    }

    fun requestLocationPermissions() {
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
            // if any permission was granted, attempt to enable my location as it will check
            // for all permissions anyways
            permissionsGranted = permissionsResult.any { it.value }
            if (permissionsGranted) enableMyLocation()
        }.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(readyMap: GoogleMap) {
        map = readyMap

        enableMyLocation()
    }
}