package com.ahmadmustafa.docconnect

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.Serializable

data class Patient(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var contactNumber: String = "",
    var cnic: String = "",
    var password: String = "",
    val picture: String? = ""
): Serializable

class signupPatient : AppCompatActivity() {
    private val NOTIFICATION_CHANNEL_ID = "RegistrationNotification"
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var cnicEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var logTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_patient)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("patients")
        sharedPreferences = getSharedPreferences("patients", Context.MODE_PRIVATE)

        usernameEditText = findViewById(R.id.username)
        emailEditText = findViewById(R.id.email)
        contactEditText = findViewById(R.id.contact)
        cnicEditText = findViewById(R.id.cnic)
        passwordEditText = findViewById(R.id.password)
        signupButton = findViewById(R.id.signup)
        logTextView = findViewById(R.id.log)

        signupButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val contact = contactEditText.text.toString().trim()
            val cnic = cnicEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(username, email, contact, cnic, password)) {
                // Check if CNIC already exists
                checkCNICExistsInPatients(cnic) { cnicExists ->
                    if (!cnicExists) {
                        // Create user with email and password
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val firebaseUser = auth.currentUser
                                    val userId = firebaseUser?.uid ?: ""

                                    // Store the patient details in the Realtime Database
                                    val patient = Patient(
                                        id = userId,
                                        name = username,
                                        email = email,
                                        contactNumber = contact,
                                        cnic = cnic,
                                        password = password
                                    )
                                    savePatientToFirebase(patient)
                                    savePatientToSharedPreferences(patient)
                                    showRegistrationSuccessNotification()
                                    // Start Home activity
                                    val intent = Intent(this, Home::class.java).apply {
                                        putExtra("patient", patient as Serializable)
                                    }
                                    startActivity(intent)
                                } else {
                                    if (task.exception?.message?.contains("email address is already in use") == true) {
                                        Toast.makeText(
                                            baseContext, "Email already in use.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            baseContext, "Authentication failed.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                    } else {
                        Toast.makeText(
                            baseContext, "CNIC already exists.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        logTextView.setOnClickListener {
            val intent = Intent(this, login::class.java).apply {
                putExtra("userType", "patient")
            }
            startActivity(intent)
        }
    }

    private fun checkCNICExistsInPatients(cnic: String, callback: (Boolean) -> Unit) {
        val query: Query = database.orderByChild("cnic").equalTo(cnic)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.exists())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }

    private fun savePatientToFirebase(patient: Patient) {
        database.child(patient.id).setValue(patient)
    }

    private fun savePatientToSharedPreferences(patient: Patient) {
        val editor = sharedPreferences.edit()
        editor.putString("id", patient.id)
        editor.putString("name", patient.name)
        editor.putString("email", patient.email)
        editor.putString("contactNumber", patient.contactNumber)
        editor.putString("cnic", patient.cnic)
        editor.putString("password", patient.password)
        editor.putString("picture", patient.picture)
        editor.apply()
    }

    private fun validateInput(username: String, email: String, contact: String, cnic: String, password: String): Boolean {
        if (username.isEmpty()) {
            usernameEditText.error = "Username is required"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Enter a valid email address"
            return false
        }

        if (contact.length != 11) {
            contactEditText.error = "Contact must be 11 digits"
            return false
        }

        if (cnic.length != 13) {
            cnicEditText.error = "CNIC must be 13 digits"
            return false
        }

        if (password.length < 8) {
            passwordEditText.error = "Password must be at least 8 characters long"
            return false
        }

        val containsUppercase = password.any { it.isUpperCase() }
        val containsLowercase = password.any { it.isLowerCase() }
        val containsDigit = password.any { it.isDigit() }

        if (!containsUppercase || !containsLowercase || !containsDigit) {
            passwordEditText.error = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
            return false
        }

        return true
    }
    private fun showRegistrationSuccessNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Registration Notification", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notification for registration success"
                enableLights(true)
                lightColor = Color.GREEN
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notifications_icon_foreground)
            .setContentTitle("Account Registeration Status")
            .setContentText("Your account has been successfully registered.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(1, builder.build())
    }
}
