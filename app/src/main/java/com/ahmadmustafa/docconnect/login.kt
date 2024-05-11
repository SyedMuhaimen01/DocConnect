package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.content.Context
import android.content.SharedPreferences
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.Serializable

class login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            signIn(email, password)
        }

        val signup = findViewById<TextView>(R.id.signup)
        signup.setOnClickListener {
            val intent = Intent(this, signupOptions::class.java)
            startActivity(intent)
        }
    }

    private fun signIn(email: String, password: String) {
        val centerRef = FirebaseDatabase.getInstance().getReference("centers")
        val professionalRef = FirebaseDatabase.getInstance().getReference("professionals")
        val patientRef = FirebaseDatabase.getInstance().getReference("patients")

        centerRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User found in centers table, consider as center admin
                        dataSnapshot.children.first().getValue(Center::class.java)
                            ?.let { startAdminHomeActivity(it) }
                    } else {
                        // User not found in centers table, check professionals table
                        professionalRef.orderByChild("email").equalTo(email)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // User found in professionals table, consider as professional
                                        dataSnapshot.children.first().getValue(Professional::class.java)
                                            ?.let { startManageAppointmentsActivity(it) }
                                    } else {
                                        // User not found in professionals table, consider as patient
                                        patientRef.orderByChild("email").equalTo(email)
                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        // User found in patients table, consider as patient
                                                        dataSnapshot.children.first().getValue(Patient::class.java)
                                                            ?.let { startHomeActivity(it) }
                                                    } else {
                                                        // User not found in any table
                                                        Toast.makeText(
                                                            baseContext, "User not found.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }

                                                override fun onCancelled(databaseError: DatabaseError) {
                                                    // Handle database error
                                                }
                                            })
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle database error
                                }
                            })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })
    }

    private fun startAdminHomeActivity(center: Center) {
        val intent = Intent(this, adminHome::class.java).apply {
            putExtra("center", center as Serializable)
        }
        startActivity(intent)
        finish()
    }

    private fun startManageAppointmentsActivity(professional: Professional) {
        val intent = Intent(this, manageAppointments::class.java).apply {
            putExtra("professional", professional as java.io.Serializable)
        }
        startActivity(intent)
        finish()
    }

    private fun startHomeActivity(patient: Patient) {
        val intent = Intent(this, Home::class.java).apply {
            putExtra("patient", patient as java.io.Serializable)
        }
        startActivity(intent)
        finish()
    }

    private fun saveLoginStatus(isLoggedIn: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }
}
