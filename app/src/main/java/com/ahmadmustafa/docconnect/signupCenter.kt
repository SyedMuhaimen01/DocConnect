package com.ahmadmustafa.docconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class signupCenter : AppCompatActivity() {
    private lateinit var centernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var logTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_center)

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
                val intent = Intent(this, adminHome::class.java)
                startActivity(intent)
            }
        }

        logTextView.setOnClickListener {
            // Handle click on "Already have an account? Sign In" text
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
}
