package com.ahmadmustafa.docconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class signupDoctor : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var cnicEditText: EditText
    private lateinit var specializationEditText: EditText
    private lateinit var affiliationEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var logTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_doctor)

        usernameEditText = findViewById(R.id.username)
        emailEditText = findViewById(R.id.email)
        contactEditText = findViewById(R.id.contact)
        cnicEditText = findViewById(R.id.cnic)
        specializationEditText = findViewById(R.id.specialization)
        affiliationEditText = findViewById(R.id.affiliation)
        passwordEditText = findViewById(R.id.password)
        signupButton = findViewById(R.id.signup)
        logTextView = findViewById(R.id.log)

        signupButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val contact = contactEditText.text.toString().trim()
            val cnic = cnicEditText.text.toString().trim()
            val specialization = specializationEditText.text.toString().trim()
            val affiliation = affiliationEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(username, email, contact, cnic, specialization, affiliation, password)) {

                val intent = Intent(this, manageAppointments::class.java)
                startActivity(intent)
            }
        }

        logTextView.setOnClickListener {
            // Handle click on "Already have an account? Sign In" text
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }
    }

    private fun validateInput(username: String, email: String, contact: String, cnic: String,
                              specialization: String, affiliation: String, password: String): Boolean {
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

        if (affiliation.isEmpty()) {
            affiliationEditText.error = "Affiliation is required"
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
}
