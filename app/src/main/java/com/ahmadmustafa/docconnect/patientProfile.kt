package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class patientProfile : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferences2: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        sharedPreferences = getSharedPreferences("patients", Context.MODE_PRIVATE)
        sharedPreferences2 = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        auth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance().getReference("patients").child(auth.currentUser?.uid ?: "")

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        val chatButton = findViewById<ImageButton>(R.id.chats)
        chatButton.setOnClickListener {
            startActivity(Intent(this, chatBox::class.java).apply { putExtra("userType", "patient")})
        }

        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            startActivity(Intent(this, map::class.java).apply { putExtra("userType", "patient")})
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, patientProfile::class.java))
        }

        // Initialize logout button
        val logoutButton = findViewById<ImageView>(R.id.logout)
        logoutButton.setOnClickListener {
            logoutUser()
        }

        val editProfile= findViewById<Button>(R.id.editProfile)
        editProfile.setOnClickListener {
            startActivity(Intent(this, editPatientProfile::class.java))
        }

        if (isConnected()) {

            fetchUserDetailsFromFirebase()
        } else {
            fetchUserDetailsFromSharedPreferences()
       }
    }

    private fun fetchUserDetailsFromFirebase() {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        Log.e("Firebase", "Current user ID: $currentUserID")

        currentUserID?.let { uid ->
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.e("Firebase", "DataSnapshot: $dataSnapshot")
                    if (dataSnapshot.exists()) {
                        // Data for the current patient exists, parse it into a Patient object
                        val patient = dataSnapshot.getValue(Patient::class.java)
                        patient?.let {
                            // Set user details or handle the data as needed
                            setUserDetails(it)
                        }
                    } else {
                        Log.d("Firebase", "No data found for current user")
                        // Handle the case where no data is found for the current user
                        // For example, show a message to the user or redirect to another screen
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
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
        Toast.makeText(this, "Data Fetched shareddddd", Toast.LENGTH_SHORT).show()
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val contactTextView = findViewById<TextView>(R.id.contactTextView)
        val cnicTextView = findViewById<TextView>(R.id.cnicTextView)
        val profileImageView = findViewById<ImageView>(R.id.profileImage)

        usernameTextView.text = username
        emailTextView.text = email
        contactTextView.text = contact
        cnicTextView.text = cnic

        // Load profile image from URL
        Glide.with(this)
            .asBitmap()
            .load(profileImageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Set rounded image
                    val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                    roundedDrawable.cornerRadius = 50f // Adjust as per your requirement
                    profileImageView.setImageDrawable(roundedDrawable)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Not implemented
                }


            })
    }

    private fun setUserDetails(patient: Patient) {
        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show()
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val contactTextView = findViewById<TextView>(R.id.contactTextView)
        val cnicTextView = findViewById<TextView>(R.id.cnicTextView)
        val profileImageView = findViewById<ImageView>(R.id.profileImage)

        usernameTextView.text = patient.name
        emailTextView.text = patient.email
        contactTextView.text = patient.contactNumber
        cnicTextView.text = patient.cnic
        Toast.makeText(this, "Data Fetched", Toast.LENGTH_SHORT).show()
        // Save fetched user details to SharedPreferences
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

    private fun logoutUser() {
        // Update SharedPreferences to mark the user as logged out
        val editor = sharedPreferences2.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()

        // Redirect the user to the login screen
        startActivity(Intent(this, login::class.java))

        finish()
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

}
