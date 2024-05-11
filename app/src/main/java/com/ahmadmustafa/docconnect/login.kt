package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class login : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //button listener for signup btn
        val signup = findViewById<TextView>(R.id.signup)
        signup.setOnClickListener {

            val intent = Intent(this, signupOptions::class.java)
            startActivity(intent)
        }
    }
}