package com.ahmadmustafa.docconnect

import android.Manifest
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

class map : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var nGoogleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var centername: String = ""
    private var user:String = ""
    private var signupCenter:String = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference


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
                val intent = Intent(this, Home::class.java).apply {
                    putExtra("userType", "patient")
                }
                startActivity(intent)
            }

            val chatButton=findViewById<ImageButton>(R.id.chats)
            chatButton.setOnClickListener {
                val intent = Intent(this, searchUsers::class.java).apply {
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
                val intent = Intent(this, centreHome::class.java)
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
        else if(user=="admin")
        {
            val homeButton=findViewById<ImageButton>(R.id.home)
            homeButton.setOnClickListener {
                val intent = Intent(this, adminHome::class.java)
                startActivity(intent)
            }

            val chatButton=findViewById<ImageButton>(R.id.chats)
            chatButton.setOnClickListener {
                Toast.makeText(this, "Feature Unavailable", Toast.LENGTH_SHORT).show()
            }

            val appointButton=findViewById<ImageButton>(R.id.appoint)
            appointButton.setOnClickListener {
                Toast.makeText(this, "Feature Unavailable", Toast.LENGTH_SHORT).show()
            }

            val profileButton=findViewById<ImageButton>(R.id.profile)
            profileButton.setOnClickListener {
                startActivity(Intent(this, login::class.java))
            }
            val mapButton=findViewById<ImageButton>(R.id.map)
            mapButton.setOnClickListener {
                startActivity(Intent(this, map::class.java).apply {
                    putExtra("userType", "admin")
                })
            }
        }
        else{
            val appointButton: ImageButton = findViewById(R.id.appoint)
            appointButton.setOnClickListener {
                showLoginDialog()
            }
            val chatButton: ImageButton = findViewById(R.id.chats)
            chatButton.setOnClickListener {
                showLoginDialog()
            }

            val profileButton: ImageButton = findViewById(R.id.profile)
            profileButton.setOnClickListener {
                showLoginDialog()
            }

            val homeButton: ImageButton = findViewById(R.id.home)
            homeButton.setOnClickListener {
                startActivity(Intent(this, Home::class.java))
            }

            val mapButton: ImageButton = findViewById(R.id.map)
            mapButton.setOnClickListener {
                startActivity(Intent(this, map::class.java))
            }
        }
    }

    private fun showLoginDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Login Required")
            .setMessage("Please login to access this feature.")
            .setPositiveButton("Login") { dialogInterface: DialogInterface, _: Int ->
                startActivity(Intent(this, login::class.java))
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .show()
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

        if (user == "center" && signupCenter == "signupCenter") {
            // Place marker at specified latitude and longitude
            val location = LatLng(latitude, longitude)
            nGoogleMap.addMarker(MarkerOptions().position(location).title(centername))
            nGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
            val intent = Intent(this, centreHome::class.java).apply {
                putExtra("centername", centername)
            }
            startActivity(intent)
        }

        auth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance().getReference("centers")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val center = snapshot.getValue(Center::class.java)

                    // Check if centerStatus is true before adding marker
                    if (center?.centerStatus == true) {
                        // Using a coroutine to perform Geocoding asynchronously
                        GlobalScope.launch(Dispatchers.IO) {
                            val geocoder = Geocoder(applicationContext)
                            try {
                                val addressList =
                                    center?.let { geocoder.getFromLocationName(it.address, 1) }
                                if (addressList != null && addressList.isNotEmpty()) {
                                    val latitude = addressList[0].latitude
                                    val longitude = addressList[0].longitude

                                    // Switch to the main thread before adding marker
                                    runOnUiThread {
                                        val location = LatLng(latitude, longitude)
                                        nGoogleMap.addMarker(MarkerOptions().position(location).title(center?.name))
                                    }
                                } else {
                                    Log.e(TAG, "No location found for address: ${center?.address}")
                                }
                            } catch (e: IOException) {
                                Log.e(TAG, "Geocoding error: ${e.message}")
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException())
            }
        })
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
