package com.ahmadmustafa.docconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class signupPatient : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var cnicEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var logTextView: TextView // Corrected type to TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_patient)

        usernameEditText = findViewById(R.id.username)
        emailEditText = findViewById(R.id.email)
        contactEditText = findViewById(R.id.contact)
        cnicEditText = findViewById(R.id.cnic)
        passwordEditText = findViewById(R.id.password)
        signupButton = findViewById(R.id.signup)
        logTextView = findViewById(R.id.log) // Corrected type to TextView

        signupButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val contact = contactEditText.text.toString().trim()
            val cnic = cnicEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(username, email, contact, cnic, password)) {
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
            }
        }

        logTextView.setOnClickListener { // Using logTextView
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }
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
}
