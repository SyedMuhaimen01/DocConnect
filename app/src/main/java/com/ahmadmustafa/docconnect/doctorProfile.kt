package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
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

class doctorProfile : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferences2: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile)
        sharedPreferences = getSharedPreferences("professionals", Context.MODE_PRIVATE)
        sharedPreferences2 = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        auth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance().getReference("professionals").child(auth.currentUser?.uid ?: "")

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, doctorViewAppointmentsList::class.java))
        }

        val chatButton = findViewById<ImageButton>(R.id.chat)
        chatButton.setOnClickListener {
            startActivity(Intent(this, chatBox::class.java).apply {
                putExtra("userType", "professional")
            })
        }

        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            startActivity(Intent(this, map::class.java).apply {
                putExtra("userType", "professional")
            })
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, doctorProfile::class.java))
        }

        val editProfileButton = findViewById<Button>(R.id.editProfile)
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, editDoctorProfile::class.java))
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
                    val professional = dataSnapshot.getValue(Professional::class.java)
                    professional?.let { setUserDetails(it) }
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
        val specialization = sharedPreferences.getString("specialization", "")
        val affiliation = sharedPreferences.getString("affiliation", "")
        val profileImageUrl = sharedPreferences.getString("picture", "")

        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val contactTextView = findViewById<TextView>(R.id.contactTextView)
        val cnicTextView = findViewById<TextView>(R.id.cnicTextView)
        val profileImageView = findViewById<ImageView>(R.id.profileImage)
        val specializationTextView = findViewById<TextView>(R.id.specializationTextView)
        val affiliationTextView = findViewById<TextView>(R.id.affiliationTextView)

        usernameTextView.text = username
        emailTextView.text = email
        contactTextView.text = contact
        cnicTextView.text = cnic
        specializationTextView.text = specialization
        affiliationTextView.text = affiliation

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

    private fun setUserDetails(professional: Professional) {
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val contactTextView = findViewById<TextView>(R.id.contactTextView)
        val cnicTextView = findViewById<TextView>(R.id.cnicTextView)
        val specializationTextView = findViewById<TextView>(R.id.specializationTextView)
        val affliationTextView = findViewById<TextView>(R.id.affiliationTextView)
        val profileImageView = findViewById<ImageView>(R.id.profileImage)

        usernameTextView.text = professional.name
        emailTextView.text = professional.email
        contactTextView.text = professional.contactNumber
        cnicTextView.text = professional.cnic
        specializationTextView.text = professional.specialization
        affliationTextView.text = professional.affiliation

        saveUserDetailsToSharedPreferences(professional)
    }

    private fun saveUserDetailsToSharedPreferences(professional: Professional) {
        val editor = sharedPreferences.edit()
        editor.putString("id", professional.id)
        editor.putString("name", professional.name)
        editor.putString("email", professional.email)
        editor.putString("contactNumber", professional.contactNumber)
        editor.putString("cnic", professional.cnic)
        editor.putString("specialization", professional.specialization)
        editor.putString("affiliation", professional.affiliation)
        editor.putBoolean("affiliationStatus", professional.affiliationStatus)
        editor.putString("password", professional.password)
        editor.putFloat("rating", professional.rating.toFloat())
        editor.putString("picture", professional.picture)
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