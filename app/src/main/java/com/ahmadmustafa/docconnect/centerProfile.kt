package com.ahmadmustafa.docconnect
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class centerProfile : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferences2: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center_profile)

        sharedPreferences = getSharedPreferences("centers", MODE_PRIVATE)
        sharedPreferences2 = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("centers")

        // Initialize UI components
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val contactTextView = findViewById<TextView>(R.id.contactTextView)
        val addressTextView = findViewById<TextView>(R.id.addressTextView)
        val categoryTextView = findViewById<TextView>(R.id.categoryTextView)
        val profileImageView = findViewById<ImageView>(R.id.profileImage)
        val editProfileButton = findViewById<Button>(R.id.editProfile)
        val logoutButton = findViewById<ImageView>(R.id.logout)
        val backButton=findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener{
            onBackPressed()
        }
        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, centreHome::class.java))
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

        // Set onClickListeners
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, editCenterProfile::class.java))
        }

        logoutButton.setOnClickListener {
            logoutUser()
        }

        fetchUserDetailsFromSharedPreferences()
    }

    private fun fetchUserDetailsFromSharedPreferences() {
        val username = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "")
        val contact = sharedPreferences.getString("contact", "")
        val address = sharedPreferences.getString("address", "")
        val category = sharedPreferences.getString("category", "")
        val profileImageUrl = sharedPreferences.getString("picture", "")

        // Set fetched data to UI components
        findViewById<TextView>(R.id.usernameTextView).text = username
        findViewById<TextView>(R.id.emailTextView).text = email
        findViewById<TextView>(R.id.contactTextView).text = contact
        findViewById<TextView>(R.id.addressTextView).text = address
        findViewById<TextView>(R.id.categoryTextView).text = category

        // Load profile image using Glide
        profileImageUrl?.let {
            Glide.with(this@centerProfile)
                .load(it)
                .circleCrop()
                .into(findViewById<ImageView>(R.id.profileImage))
        }
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
