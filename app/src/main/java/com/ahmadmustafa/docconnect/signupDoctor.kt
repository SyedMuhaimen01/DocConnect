package com.ahmadmustafa.docconnect

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.Serializable

data class Professional(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var contactNumber: String = "",
    var cnic: String = "",
    var specialization: String = "",
    var affiliation: String = "",
    var affiliationStatus: Boolean = false,
    var password: String = "",
    var rating: Double = 0.0, // Updated attribute for rating
    val picture: String? = ""
) : Serializable

class signupDoctor : AppCompatActivity() {
    private val NOTIFICATION_CHANNEL_ID = "RegistrationNotification"
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var cnicEditText: EditText
    private lateinit var specializationEditText: EditText
    private lateinit var affiliationSpinner: Spinner
    private lateinit var passwordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var logTextView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_doctor)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("professionals")
        sharedPreferences = getSharedPreferences("com.ahmadmustafa.docconnect.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)

        usernameEditText = findViewById(R.id.username)
        emailEditText = findViewById(R.id.email)
        contactEditText = findViewById(R.id.contact)
        cnicEditText = findViewById(R.id.cnic)
        specializationEditText = findViewById(R.id.specialization)
        affiliationSpinner = findViewById(R.id.affiliationSpinner)
        passwordEditText = findViewById(R.id.password)
        signupButton = findViewById(R.id.signup)
        logTextView = findViewById(R.id.log)

        // Populate Spinner with Center Names
        populateAffiliationSpinner()

        signupButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val contact = contactEditText.text.toString().trim()
            val cnic = cnicEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val specialization = specializationEditText.text.toString().trim()
            val affiliation = affiliationSpinner.selectedItem.toString()

            if (validateInput(username, email, contact, cnic, specialization, affiliation, password)) {
                // Check if CNIC already exists
                checkCNICExistsInProfessionals(cnic) { cnicExists ->
                    if (!cnicExists) {

                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val firebaseUser = auth.currentUser
                                    val userId = firebaseUser?.uid ?: ""

                                    // Store the professional details in the Realtime Database
                                    val professional = Professional(
                                        id = userId,
                                        name = username,
                                        email = email,
                                        contactNumber = contact,
                                        cnic = cnic,
                                        specialization = specialization,
                                        affiliation = affiliation,
                                        affiliationStatus = false,
                                        password = password,
                                        rating = 0.0
                                    )
                                    saveProfessionalToFirebase(professional)
                                    saveProfessionalToSharedPreferences(professional)
                                    showRegistrationSuccessNotification()
                                    // Start Home activity
                                    val intent = Intent(this, manageAppointments::class.java).apply {
                                        putExtra("professional", professional as Serializable)
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
                putExtra("userType", "professional")
            }
            startActivity(intent)
        }
    }

    private fun populateAffiliationSpinner() {
        val centerNamesWithAddress: MutableList<String> = mutableListOf()
        val centerRef = FirebaseDatabase.getInstance().getReference("centers")
        centerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val centerName = snapshot.child("name").getValue(String::class.java)
                    val centerAddress = snapshot.child("address").getValue(String::class.java)
                    if (centerName != null && centerAddress != null) {
                        val centerInfo = "$centerName | $centerAddress"
                        centerNamesWithAddress.add(centerInfo)
                    }
                }
                // Add "Not right now" option
                centerNamesWithAddress.add("Not right now")
                // Create an ArrayAdapter and set it to the spinner
                val adapter = ArrayAdapter(this@signupDoctor, android.R.layout.simple_spinner_item, centerNamesWithAddress)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                affiliationSpinner.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }


    private fun checkCNICExistsInProfessionals(cnic: String, callback: (Boolean) -> Unit) {
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

    private fun saveProfessionalToFirebase(professional: Professional) {
        database.child(professional.id).setValue(professional)
    }

    private fun saveProfessionalToSharedPreferences(professional: Professional) {
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

    private fun validateInput(
        username: String, email: String, contact: String, cnic: String,
        specialization: String, affiliation: String, password: String
    ): Boolean {
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

        if (specialization.isEmpty()) {
            specializationEditText.error = "Specialization is required"
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
            passwordEditText.error =
                "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
            return false
        }

        return true
    }

    private fun showRegistrationSuccessNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Registration Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notification for registration success"
                enableLights(true)
                lightColor = Color.GREEN
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.notifications_icon_foreground)
            .setContentTitle("Account Registration Status")
            .setContentText("Your account has been successfully registered.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(1, builder.build())
    }
}
