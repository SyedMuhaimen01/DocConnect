package com.ahmadmustafa.docconnect

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*

class login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (email=="admin" && password=="admin") {
                startActivity(Intent(this, adminHome::class.java))
                finish()
            }
            else {
                signIn(email, password)
            }
        }

        val signup = findViewById<TextView>(R.id.signup)
        signup.setOnClickListener {
            val intent = Intent(this, signupOptions::class.java)
            startActivity(intent)
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    user?.let {
                        // Retrieve user details from the database based on user ID
                        val userId = user.uid
                        getUserType(userId) { userType ->
                            if (userType.isNotEmpty()) {
                                saveLoginStatus(true, userType)
                                startNextActivity(userType)
                            } else {
                                Toast.makeText(
                                    baseContext, "User type not found.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun getUserType(userId: String, callback: (String) -> Unit) {
        val centerRef = FirebaseDatabase.getInstance().getReference("centers")
        val professionalRef = FirebaseDatabase.getInstance().getReference("professionals")
        val patientRef = FirebaseDatabase.getInstance().getReference("patients")

        var userType = ""

        // Check if the user exists in any of the tables
        centerRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    userType = "center"
                    callback(userType)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })

        professionalRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    userType = "professional"
                    callback(userType)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })

        patientRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    userType = "patient"
                    callback(userType)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun startNextActivity(userType: String) {
        when (userType) {
            "center" -> {
                startActivity(Intent(this, centreHome::class.java))
            }
            "professional" -> {
                startActivity(Intent(this, doctorViewAppointmentsList::class.java))
            }
            "patient" -> {
                startActivity(Intent(this, Home::class.java).apply {
                    putExtra("userType", "patient")
                })
            }
            else -> {
                startActivity(Intent(this, Home::class.java))
                // Handle unexpected user type
            }
        }
        finish()
    }

    private fun saveLoginStatus(isLoggedIn: Boolean, userType: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.putString("userType", userType)
        editor.apply()
    }
}
