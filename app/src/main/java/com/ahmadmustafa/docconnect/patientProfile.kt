package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class patientProfile : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var sharedPreferences2: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)
        sharedPreferences2 = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        sharedPreferences = getSharedPreferences("patients", Context.MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("patients").child(auth.currentUser?.uid ?: "")
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, Home::class.java).apply {
                putExtra("userType", "patient")
            })
        }

        val chatButton = findViewById<ImageButton>(R.id.chats)
        chatButton.setOnClickListener {
            startActivity(Intent(this, chatBox::class.java).apply { putExtra("userType", "patient") })
        }

        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            startActivity(Intent(this, map::class.java).apply { putExtra("userType", "patient") })
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            // Already on the profile page
        }

        var logoutButton = findViewById<ImageView>(R.id.logout)
        logoutButton.setOnClickListener {
            logoutUser()

        }
        val appointButton = findViewById<ImageButton>(R.id.appoint)
        appointButton.setOnClickListener {
            startActivity(
                Intent(this, manageAppointments::class.java).apply {
                    putExtra("userType", "patient")
                })
        }

        val editProfile = findViewById<Button>(R.id.editProfile)
        editProfile.setOnClickListener {
            startActivity(Intent(this, editPatientProfile::class.java))
        }

        //if (isConnected()) {
          //  fetchUserDetailsFromFirebase()
        //} else {
            fetchUserDetailsFromSharedPreferences()
        //}
    }

    private fun fetchUserDetailsFromFirebase() {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

        currentUserID?.let { uid ->
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val patient = dataSnapshot.getValue(Patient::class.java)
                        patient?.let {
                            setUserDetails(it)
                        }
                    } else {
                        Log.e("Firebase", "No data found for current user")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error fetching data: ${databaseError.message}")
                }
            })
        }
    }

    private fun fetchUserDetailsFromSharedPreferences() {
        val username = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "")
        val contact = sharedPreferences.getString("contactNumber", "")
        val cnic = sharedPreferences.getString("cnic", "")
        val profileImageUrl = sharedPreferences.getString("picture", "")

        findViewById<TextView>(R.id.usernameTextView).text = username
        findViewById<TextView>(R.id.emailTextView).text = email
        findViewById<TextView>(R.id.contactTextView).text = contact
        findViewById<TextView>(R.id.cnicTextView).text = cnic

        profileImageUrl?.let {
            loadProfileImage(it)
        }
    }

    private fun loadProfileImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .circleCrop()
            .into(findViewById<ImageView>(R.id.profileImage))
    }

    private fun setUserDetails(patient: Patient) {
        Toast.makeText(this, "dataset", Toast.LENGTH_SHORT).show()
        findViewById<TextView>(R.id.usernameTextView).text = patient.name
        findViewById<TextView>(R.id.emailTextView).text = patient.email
        findViewById<TextView>(R.id.contactTextView).text = patient.contactNumber
        findViewById<TextView>(R.id.cnicTextView).text = patient.cnic

        patient.picture?.let {
            loadProfileImage(it)
        }

        saveUserDetailsToSharedPreferences(patient)
    }

    private fun saveUserDetailsToSharedPreferences(patient: Patient) {
        val editor = sharedPreferences.edit()
        editor.putString("name", patient.name)
        editor.putString("email", patient.email)
        editor.putString("contactNumber", patient.contactNumber)
        editor.putString("cnic", patient.cnic)
        editor.putString("picture", patient.picture)
        editor.apply()
    }

    private fun isConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
    private fun logoutUser() {
        // Update SharedPreferences to mark the user as logged out
        sharedPreferences.edit().clear().apply()
        val editor = sharedPreferences2.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()
        auth.signOut()
        // Redirect the user to the login screen
        startActivity(Intent(this, login::class.java))

        finish()
    }
}
