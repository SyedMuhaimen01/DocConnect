package com.ahmadmustafa.docconnect
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.Serializable
import java.util.Locale

data class Center(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var contactNumber: String = "",
    var address: String = "",
    var category: String = "",
    var password: String = "",
    var centerStatus: Boolean = false,
    val picture: String? = ""
): Serializable {
    // Default constructor
    constructor() : this("", "", "", "", "", "", "", false, null)
}

class signupCenter : AppCompatActivity() {
    private val NOTIFICATION_CHANNEL_ID = "RegistrationNotification"
    private lateinit var centernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var logTextView: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_center)

        auth = FirebaseAuth.getInstance()

        centernameEditText = findViewById(R.id.centername)
        emailEditText = findViewById(R.id.email)
        contactEditText = findViewById(R.id.contact)
        addressEditText = findViewById(R.id.address)
        typeEditText = findViewById(R.id.type)
        passwordEditText = findViewById(R.id.password)
        signupButton = findViewById(R.id.signup)
        logTextView = findViewById(R.id.log)

        signupButton.setOnClickListener {
            val centername = centernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val contact = contactEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()
            val type = typeEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(centername, email, contact, address, type, password)) {
                checkCenterExists(centername, address) { centerExists ->
                    if (!centerExists) {
                        // Register center with Firebase Authentication
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    // Registration successful, save center details to Firebase database
                                    val user = auth.currentUser
                                    val center = Center(
                                        user?.uid ?: "",
                                        centername,
                                        email,
                                        contact,
                                        address,
                                        type,
                                        password,
                                        centerStatus = false
                                    )
                                    saveCenterToFirebase(center)
                                    saveCenterToSharedPreferences(center)
                                    showRegistrationSuccessNotification()
                                    val geocoder = Geocoder(this, Locale.getDefault())
                                    val addresses = geocoder.getFromLocationName(address, 1)
                                    if (addresses != null) {
                                        if (addresses.isNotEmpty()) {
                                            val latitude = addresses?.get(0)?.latitude
                                            val longitude = addresses?.get(0)?.longitude

                                            // Create an intent to pass the coordinates to the map activity
                                            val intent = Intent(this, map::class.java).apply {
                                                putExtra("latitude", latitude)
                                                putExtra("longitude", longitude)
                                                putExtra("center", centername)
                                            }
                                            startActivity(intent)
                                        } else {
                                            showToast("Failed to geocode address.")
                                        }
                                    }
                                    val intent = Intent(this, adminHome::class.java).apply {
                                        putExtra("center", center as Serializable)
                                    }
                                    startActivity(intent)
                                } else {
                                    // Registration failed
                                    showToast("Registration Failed. Failed to register center. Please try again later.")
                                }
                            }
                    } else {
                        showToast("Registration Failed Center already exists.")
                    }
                }
            }
        }

        logTextView.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }
    }
    private fun validateInput(centername: String, email: String, contact: String, address: String,
                              type: String, password: String): Boolean {
        if (centername.isEmpty()) {
            centernameEditText.error = "Center name is required"
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

        if (address.isEmpty()) {
            addressEditText.error = "Address is required"
            return false
        }

        if (type.isEmpty()) {
            typeEditText.error = "Category is required"
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

    private fun checkCenterExists(name: String, address: String, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("centers")

        reference.orderByChild("name").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val center = snapshot.getValue(Center::class.java)
                        if (center != null && center.address == address) {
                            // Center with same name and address found
                            callback.invoke(true)
                            return
                        }
                    }
                    // No center found with the same name and address
                    callback.invoke(false)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Error occurred while fetching data
                    showToast("Error. Failed to check center existence: ${databaseError.message}")
                    callback.invoke(false)
                }
            })
    }


    private fun saveCenterToFirebase(center: Center) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("centers")

        reference.child(center.id).setValue(center)
            .addOnSuccessListener {
                // Call saveCenter to save details to SharedPreferences after Firebase save is successful
            }
            .addOnFailureListener { e ->
                showToast("Error. Failed to save center: ${e.message}")
            }
    }

    private fun saveCenterToSharedPreferences(center: Center) {
        // Save center details to Firebase
        saveCenterToFirebase(center)

        // Save center details to SharedPreferences
        val sharedPreferences = getSharedPreferences("CenterPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("centerId", center.id)
        editor.putString("centerName", center.name)
        editor.putString("centerEmail", center.email)
        editor.putString("centerContact", center.contactNumber)
        editor.putString("centerAddress", center.address)
        editor.putString("centerCategory", center.category)
        editor.putString("centerPassword", center.password)
        editor.putBoolean("centerStatus", center.centerStatus)
        editor.putString("centerPicture", center.picture)
        editor.apply()
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
            .setContentTitle("Account Registeration Status")
            .setContentText("Your account has been successfully registered.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(1, builder.build())
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
