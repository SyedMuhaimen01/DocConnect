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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        auth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance().getReference("patients").child(auth.currentUser?.uid ?: "")

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        val chatButton = findViewById<ImageButton>(R.id.chats)
        chatButton.setOnClickListener {
            startActivity(Intent(this, chatBox::class.java))
        }

        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            startActivity(Intent(this, map::class.java))
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

        if (isConnected()) {
            fetchUserDetailsFromFirebase()
        } else {
            fetchUserDetailsFromSharedPreferences()
        }
    }

    private fun fetchUserDetailsFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val patient = dataSnapshot.getValue(Patient::class.java)
                    patient?.let { setUserDetails(it) }
                } else {
                    // Handle the case where no data is found for the current user
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun fetchUserDetailsFromSharedPreferences() {
        val username = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "")
        val contact = sharedPreferences.getString("contactNumber", "")
        val cnic = sharedPreferences.getString("cnic", "")
        val profileImageUrl = sharedPreferences.getString("profileImageUrl", "")

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
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val contactTextView = findViewById<TextView>(R.id.contactTextView)
        val cnicTextView = findViewById<TextView>(R.id.cnicTextView)
        val profileImageView = findViewById<ImageView>(R.id.profileImage)

        usernameTextView.text = patient.name
        emailTextView.text = patient.email
        contactTextView.text = patient.contactNumber
        cnicTextView.text = patient.cnic

        // Save fetched user details to SharedPreferences
        saveUserDetailsToSharedPreferences(patient)
    }

    private fun saveUserDetailsToSharedPreferences(patient: Patient) {
        val editor = sharedPreferences.edit()
        editor.putString("name", patient.name)
        editor.putString("email", patient.email)
        editor.putString("contactNumber", patient.contactNumber)
        editor.putString("cnic", patient.cnic)
        editor.putString("profileImageUrl", patient.picture)
        editor.apply()
    }

    private fun logoutUser() {
        // Update SharedPreferences to mark the user as logged out
        val editor = sharedPreferences.edit()
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
