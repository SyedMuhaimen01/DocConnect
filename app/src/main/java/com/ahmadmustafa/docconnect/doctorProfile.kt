package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class doctorProfile : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var sharedPreferences2: SharedPreferences

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile)

        sharedPreferences2 = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        sharedPreferences = getSharedPreferences("professionals", Context.MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("professionals").child(auth.currentUser?.uid ?: "")
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, doctorViewAppointmentsList::class.java).apply {
                putExtra("userType", "professional")
            })
        }

        val chatButton = findViewById<ImageButton>(R.id.chat)
        chatButton.setOnClickListener {
            startActivity(Intent(this, chatBox::class.java).apply {
                putExtra("userType", "professional") })
        }

        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            startActivity(Intent(this, map::class.java).apply {
                putExtra("userType", "professional") })
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, doctorProfile::class.java))
        }

        var logoutButton = findViewById<ImageView>(R.id.logout)
        logoutButton.setOnClickListener {
            logoutUser()

        }
        val appointButton = findViewById<ImageButton>(R.id.appoint)
        appointButton.setOnClickListener {
            startActivity(
                Intent(this, doctorViewAppointmentsList::class.java).apply {
                    putExtra("userType", "professional")
                })
        }

        val editProfile = findViewById<Button>(R.id.editProfile)
        editProfile.setOnClickListener {
            startActivity(Intent(this, editDoctorProfile::class.java))
        }

        val backButton= findViewById<ImageView>(R.id.back)
        backButton.setOnClickListener {
            onBackPressed()
        }

        //if (isConnected()) {
         //   fetchUserDetailsFromFirebase()
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
                        val professional = dataSnapshot.getValue(Professional::class.java)
                        professional?.let {
                            setUserDetails(it)
                        }
                    } else {
                        Log.d("Firebase", "No data found for current user")
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
        val specialization= sharedPreferences.getString("specialization", "")
        val affiliation= sharedPreferences.getString("affiliation", "")
        val profileImageUrl = sharedPreferences.getString("picture", "")

        findViewById<TextView>(R.id.usernameTextView).text = username
        findViewById<TextView>(R.id.emailTextView).text = email
        findViewById<TextView>(R.id.contactTextView).text = contact
        findViewById<TextView>(R.id.cnicTextView).text = cnic
        findViewById<TextView>(R.id.specializationTextView).text = specialization
        findViewById<TextView>(R.id.affiliationTextView).text = affiliation

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

    private fun setUserDetails(professional: Professional) {
        findViewById<TextView>(R.id.usernameTextView).text = professional.name
        findViewById<TextView>(R.id.emailTextView).text = professional.email
        findViewById<TextView>(R.id.contactTextView).text = professional.contactNumber
        findViewById<TextView>(R.id.cnicTextView).text = professional.cnic
        findViewById<TextView>(R.id.specializationTextView).text = professional.specialization
        findViewById<TextView>(R.id.affiliationTextView).text = professional.affiliation

        professional.picture?.let {
            loadProfileImage(it)
        }

        saveUserDetailsToSharedPreferences(professional)
    }

    private fun saveUserDetailsToSharedPreferences(professional: Professional) {
        val editor = sharedPreferences.edit()
        editor.putString("name", professional.name)
        editor.putString("email", professional.email)
        editor.putString("contactNumber", professional.contactNumber)
        editor.putString("cnic", professional.cnic)
        editor.putString("specialization", professional.specialization)
        editor.putString("affiliation", professional.affiliation)
        editor.putString("picture", professional.picture)
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
        val editor = sharedPreferences2.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()
        auth.signOut()
        // Redirect the user to the login screen
        startActivity(Intent(this, login::class.java))

        finish()
    }

}
