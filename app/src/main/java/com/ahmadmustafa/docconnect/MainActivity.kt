package com.ahmadmustafa.docconnect
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE)

        val swipeButton = findViewById<ImageView>(R.id.swipe)
        swipeButton.setOnClickListener {
            // Check if the user is already logged in
            if (isLoggedIn()) {
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
            } else {
                showLoginDialog()
            }
        }
    }

    private fun showLoginDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Choose an option:")
            .setPositiveButton("Continue as Guest") { dialog, which ->
                // Handle "Continue as Guest" option
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Login") { dialog, which ->
                // Handle "Login" option
                val intent = Intent(this, login::class.java)
                startActivity(intent)
            }
            .show()
    }

    // Function to check if the user is logged in
    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }
}
