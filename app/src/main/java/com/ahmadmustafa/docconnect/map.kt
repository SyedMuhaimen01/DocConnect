package com.ahmadmustafa.docconnect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class map : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var nGoogleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        Places.initialize(applicationContext, "AIzaSyCrmREiQ30QCg4mQVmdFdeMFTLeA1k8LA8")
        placesClient = Places.createClient(this)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.gmaps) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        nGoogleMap = googleMap

        // Enable the "My Location" layer if permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun enableMyLocation() {
        // Enable the "My Location" layer
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        nGoogleMap.isMyLocationEnabled = true

        // Request location updates
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // Got last known location
                location?.let {
                    // Move camera to user's current location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    nGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))

                    // Search for medical centers nearby
                    searchMedicalCentersNearby(currentLatLng)
                }
            }
    }

    private fun searchMedicalCentersNearby(location: LatLng) {
        // Specify the types of places to search for
        val placeTypes = listOf(
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.TYPES
        )

        // Create a FindCurrentPlaceRequest to query nearby places
        val request = FindCurrentPlaceRequest.newInstance(placeTypes)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        placesClient.findCurrentPlace(request).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val response = task.result
                response?.placeLikelihoods?.forEach { placeLikelihood ->
                    val place = placeLikelihood.place
                    Log.d("NearbyPlaces", "Place: ${place.name}, Likelihood: ${placeLikelihood.likelihood}")
                    val placeLatLng = place.latLng
                    placeLatLng?.let {
                        // Add marker for each nearby medical center
                        nGoogleMap.addMarker(
                            MarkerOptions()
                                .position(placeLatLng)
                                .title(place.name)
                        )
                    }
                }
            } else {
                val exception = task.exception
                Log.e("NearbyPlaces", "Exception: $exception")
                // Handle failure
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}