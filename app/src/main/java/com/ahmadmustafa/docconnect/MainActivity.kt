package com.ahmadmustafa.docconnect

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val swipeButton = findViewById<ImageView>(R.id.swipe)
        swipeButton.setOnClickListener {
            showLoginDialog()
        }
    }

    private fun showLoginDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Choose an option:")
            .setPositiveButton("Continue as Guest") { dialog, which ->
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Login") { dialog, which ->
                val intent = Intent(this, login::class.java)
                startActivity(intent)
            }
            .show()
    }
}
