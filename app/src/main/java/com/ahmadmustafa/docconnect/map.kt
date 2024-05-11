package com.ahmadmustafa.docconnect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
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
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var centername: String = ""
    private var user:String = ""
    private var signupCenter:String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        Places.initialize(applicationContext, "AIzaSyCrmREiQ30QCg4mQVmdFdeMFTLeA1k8LA8")
        placesClient = Places.createClient(this)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.gmaps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Receive latitude and longitude values from intent
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        centername = intent.getStringExtra("centername").toString()
        user = intent.getStringExtra("userType").toString()
        signupCenter = intent.getStringExtra("signupCenter").toString()

        //varying directions based on type of user

        if(user=="patient")
        {
            val homeButton=findViewById<ImageButton>(R.id.home)
            homeButton.setOnClickListener {
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
            }

            val chatButton=findViewById<ImageButton>(R.id.chats)
            chatButton.setOnClickListener {
                val intent = Intent(this, chatBox::class.java).apply {
                    putExtra("userType", "patient")
                }
                startActivity(intent)
            }

            val appointButton=findViewById<ImageButton>(R.id.appoint)
            appointButton.setOnClickListener {
                val intent = Intent(this, manageAppointments::class.java)
                startActivity(intent)
            }

            val profileButton=findViewById<ImageButton>(R.id.profile)
            profileButton.setOnClickListener {
                val intent = Intent(this, patientProfile::class.java)
                startActivity(intent)
            }
            val mapButton=findViewById<ImageButton>(R.id.map)
            mapButton.setOnClickListener {
                val intent = Intent(this, map::class.java).apply {
                    putExtra("userType", "patient")
                }
                startActivity(intent)
            }
        }
        else if(user=="professional")
        {
            val homeButton=findViewById<ImageButton>(R.id.home)
            homeButton.setOnClickListener {
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
            }

            val chatButton=findViewById<ImageButton>(R.id.chats)
            chatButton.setOnClickListener {
                val intent = Intent(this, chatBox::class.java).apply {
                    putExtra("userType", "professional")
                }
                startActivity(intent)
            }

            val appointButton=findViewById<ImageButton>(R.id.appoint)
            appointButton.setOnClickListener {
                val intent = Intent(this, manageAppointments::class.java)
                startActivity(intent)
            }

            val profileButton=findViewById<ImageButton>(R.id.profile)
            profileButton.setOnClickListener {
                val intent = Intent(this, doctorProfile::class.java)
                startActivity(intent)
            }
            val mapButton=findViewById<ImageButton>(R.id.map)
            mapButton.setOnClickListener {
                val intent = Intent(this, map::class.java).apply {
                    putExtra("userType", "professional")
                }
                startActivity(intent)
            }
        }

        else if(user=="center")
        {
            val homeButton=findViewById<ImageButton>(R.id.home)
            homeButton.setOnClickListener {
                val intent = Intent(this, adminHome::class.java)
                startActivity(intent)
            }

            val chatButton=findViewById<ImageButton>(R.id.chats)
            chatButton.setOnClickListener {
                val intent = Intent(this, chatBox::class.java).apply {
                    putExtra("userType", "center")
                }
                startActivity(intent)
            }

            val appointButton=findViewById<ImageButton>(R.id.appoint)
            appointButton.setOnClickListener {
                val intent = Intent(this, manageAppointments::class.java)
                startActivity(intent)
            }

            val profileButton=findViewById<ImageButton>(R.id.profile)
            profileButton.setOnClickListener {
                val intent = Intent(this, centerProfile::class.java)
                startActivity(intent)
            }
            val mapButton=findViewById<ImageButton>(R.id.map)
            mapButton.setOnClickListener {
                val intent = Intent(this, map::class.java).apply {
                    putExtra("userType", "center")
                }
                startActivity(intent)
            }
        }

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

        if(user=="center" && signupCenter=="signupCenter"){
            // Place marker at specified latitude and longitude
            val location = LatLng(latitude, longitude)
            nGoogleMap.addMarker(MarkerOptions().position(location).title(centername))
            nGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
            val intent = Intent(this, adminHome::class.java).apply {
                putExtra("centername", centername)
            }
            startActivity(intent)
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
            return
        }
        nGoogleMap.isMyLocationEnabled = true

        // Move camera to user's current location
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    nGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
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
