package com.udacity.location_reminders.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.location.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.udacity.location_reminders.R


class GoogleMapHelper(private val fragment: Fragment) : OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val activity: Activity
    private val mapFragment: SupportMapFragment
    private var permissionsGranted = false
    var onMapCallBack: (() -> Unit)? = null
    private var currentMarker: Marker? = null
    private var currentGeofenceCircle: Circle? = null
    val currentPlaceName = MutableLiveData("")
    private lateinit var geocoder: Geocoder

    init {
        activity = fragment.requireActivity()
        mapFragment = fragment.childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun updateGeofenceRadius(newRadius: Double) {
        currentGeofenceCircle?.let {
            it.radius = newRadius
        }
    }

    fun getMap() = map

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        // Return from permissions request may call this before map is ready
        if (!this::map.isInitialized) return

        if (permissionsGranted) {
            map.isMyLocationEnabled = true
            moveCameraToLocation(getLastKnownLocation())
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        if (!permissionsGranted) return null
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = locationManager.allProviders
        var currentProvider = locationManager.getBestProvider(Criteria(), true) ?: providers[0]

        while (providers.size > 0) {
            if (locationManager.isProviderEnabled(currentProvider)) {
                val lastKnownLocation = locationManager.getLastKnownLocation(currentProvider)
                if (lastKnownLocation != null) {
                    return lastKnownLocation
                }
            }
            providers.remove(currentProvider)

            // prefer GPS if not already taken, then test other providers
            currentProvider = if (providers.contains("gps")) {
                "gps"
            } else {
                providers[0]
            }
        }
        return null
    }

    fun moveCameraToLocation(ltLng: LatLng) = map.animateCamera(
        CameraUpdateFactory.newLatLngZoom(ltLng, 16F)
    )

    private fun moveCameraToLocation(loc: Location?) = loc?.apply {
        moveCameraToLocation(LatLng(loc.latitude, loc.longitude))
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

    fun addMarker(
        options: MarkerOptions,
        name: String? = null,
        roundGeofenceRadius: Double? = null,
        temporary: Boolean = false
    ): Marker? {
        if (!this::map.isInitialized) return null

        val marker = map.addMarker(options) ?: return null

        val roundGeofence = if (roundGeofenceRadius != null) {
            map.addCircle(
                CircleOptions()
                    .center(options.position)
                    .strokeColor(Color.DKGRAY)
                    // Radius in meters
                    .radius(roundGeofenceRadius)
            )
        } else null

        if (temporary) {
            if (roundGeofence != null) {
                currentGeofenceCircle?.remove()
                currentGeofenceCircle = roundGeofence
            }
            currentMarker?.remove()
            currentMarker = marker
        }

        if (name.isNullOrBlank()) {
            findCurrentPlaceName(marker.position)
        } else {
            currentPlaceName.postValue(name)
        }

        return marker
    }

    private fun findCurrentPlaceName(position: LatLng) {
        var place: String? = null

        val addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1)
        addresses?.let {
            if (it.isNotEmpty()) {
                val address = it.first()
                place = address.getAddressLine(address.maxAddressLineIndex)
            }
        }

        currentPlaceName.postValue(
            place ?: fragment.getString(
                R.string.unknown_location,
                position.latitude,
                position.longitude
            )
        )
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    override fun onMapReady(readyMap: GoogleMap) {
        map = readyMap

        // DONE: add style to the map
        map.setMapStyle(
            MapStyleOptions
                .loadRawResourceStyle(fragment.requireContext(), R.raw.style_json)
        )

        geocoder = Geocoder(fragment.requireContext())

        enableMyLocation()

        onMapCallBack?.invoke()
    }

    fun mapTypeNormal() {
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    fun mapTypeHybrid() {
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
    }

    fun mapTypeStatellite() {
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
    }

    fun mapTypeTerrain() {
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN
    }
}