package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Home : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
        val chatButton = findViewById<ImageButton>(R.id.chats)
        chatButton.setOnClickListener {
            val intent = Intent(this, chatBox::class.java)
            startActivity(intent)
        }
        val mapButton=findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            val intent = Intent(this, map::class.java)
            startActivity(intent)
        }

        val profileButton=findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            val intent = Intent(this, patientProfile::class.java)
            startActivity(intent)
        }
    }
}