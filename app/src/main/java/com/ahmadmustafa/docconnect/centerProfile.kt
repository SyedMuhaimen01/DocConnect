package com.ahmadmustafa.docconnect

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class centerProfile : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferences2: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center_profile)

        sharedPreferences = getSharedPreferences("centers", Context.MODE_PRIVATE)
        sharedPreferences2 = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        auth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance().getReference("centers")

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, centreHome::class.java))
        }

        val chatButton = findViewById<ImageButton>(R.id.chat)
        chatButton.setOnClickListener {
            startActivity(Intent(this, chatBox::class.java).apply {
                putExtra("userType", "center")
            })
        }

        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            startActivity(Intent(this, map::class.java).apply {
                putExtra("userType", "center")
            })
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, centerProfile::class.java))
        }

        val editProfileButton = findViewById<Button>(R.id.editProfile)
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, editCenterProfile::class.java))
        }

        // Initialize logout button
        val logoutButton = findViewById<ImageView>(R.id.logout)
        logoutButton.setOnClickListener {
            logoutUser()
        }

        fetchUserDetailsFromFirebase()
    }

    private fun fetchUserDetailsFromFirebase() {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            databaseReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val center = dataSnapshot.getValue(Center::class.java)
                        center?.let { setUserDetails(it) }
                    } else {
                        // Handle the case where no data is found for the current user
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })
        } ?: run {
            // Handle the case where user is not logged in
            // No user is signed in, redirect to login screen
            startActivity(Intent(this, login::class.java))
            finish()
        }
    }

    private fun setUserDetails(center: Center) {
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val contactTextView = findViewById<TextView>(R.id.contactTextView)
        val addressTextView=findViewById<TextView>(R.id.addressTextView)
        val categoryTextView=findViewById<TextView>(R.id.categoryTextView)

        val profileImageView = findViewById<ImageView>(R.id.profileImage)

        usernameTextView.text = center.name
        emailTextView.text = center.email
        contactTextView.text = center.contactNumber
        addressTextView.text=center.address
        categoryTextView.text=center.category
        saveUserDetailsToSharedPreferences(center)
    }

    private fun saveUserDetailsToSharedPreferences(center: Center) {
        val editor = sharedPreferences.edit()
        editor.putString("centerId", center.id)
        editor.putString("name", center.name)
        editor.putString("email", center.email)
        editor.putString("contact", center.contactNumber)
        editor.putString("address", center.address)
        editor.putString("category", center.category)
        editor.putString("password", center.password)
        editor.putBoolean("status", center.centerStatus)
        editor.putString("picture", center.picture)
        editor.putString("certificate", center.certificate)
        editor.apply()
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

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // No user is signed in, redirect to login screen
            startActivity(Intent(this, login::class.java))
            finish()
        }
    }
}
