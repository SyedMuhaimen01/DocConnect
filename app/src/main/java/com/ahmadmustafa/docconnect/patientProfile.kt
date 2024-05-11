package com.ahmadmustafa.docconnect

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts

import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class patientProfile : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferences2: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var profileImage: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImage ->
            uploadImageToFirebaseStorage(selectedImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        sharedPreferences = getSharedPreferences("patients", Context.MODE_PRIVATE)
        sharedPreferences2 = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        auth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance().getReference("patients").child(auth.currentUser?.uid ?: "")
        storageReference = FirebaseStorage.getInstance().reference

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        val chatButton = findViewById<ImageButton>(R.id.chats)
        chatButton.setOnClickListener {
            startActivity(Intent(this, chatBox::class.java).apply { putExtra("userType", "patient")})
        }

        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {

            startActivity(Intent(this, map::class.java).apply { putExtra("userType", "patient")})

            startActivity(Intent(this, Map::class.java))

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, patientProfile::class.java))
        }

        val logoutButton = findViewById<ImageView>(R.id.logout)
        logoutButton.setOnClickListener {
            logoutUser()
        }


        val editProfileButton = findViewById<Button>(R.id.editprofile)
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, editPatientProfile::class.java))
        }

        profileImage = findViewById(R.id.profileImage)
        profileImage.setOnClickListener {
            openFileChooser()
        }


        if (isConnected()) {

            fetchUserDetailsFromFirebase()
        } else {
            fetchUserDetailsFromSharedPreferences()
       }
    }

    private fun fetchUserDetailsFromFirebase() {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            val userRef = databaseReference
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val contact = snapshot.child("contactNumber").getValue(String::class.java)
                    val imageUrl = snapshot.child("picture").getValue(String::class.java)

                    Log.d("patientProfile", "Username: $username, Email: $email, Contact: $contact, Image URL: $imageUrl")

                    username?.let { findViewById<TextView>(R.id.usernameTextView).text = it }
                    email?.let { findViewById<TextView>(R.id.emailTextView).text = it }
                    contact?.let { findViewById<TextView>(R.id.contactTextView).text = it }

                    imageUrl?.let { url ->
                        Glide.with(this@patientProfile)
                            .load(url)
                            .circleCrop()
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.e("patientProfile", "Failed to load image: $e")
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.d("patientProfile", "Image loaded successfully")
                                    return false
                                }
                            })
                            .into(findViewById(R.id.profileImage))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseDatabase", "Error fetching user profile: ${error.message}")

                }
            })
        }
    }


    private fun fetchUserDetailsFromSharedPreferences() {
        val username = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "")
        val contact = sharedPreferences.getString("contactNumber", "")

        val cnic = sharedPreferences.getString("cnic", "")
        val profileImageUrl = sharedPreferences.getString("picture", "")
        Toast.makeText(this, "Data Fetched shareddddd", Toast.LENGTH_SHORT).show()
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val contactTextView = findViewById<TextView>(R.id.contactTextView)
        val cnicTextView = findViewById<TextView>(R.id.cnicTextView)
        val profileImageView = findViewById<ImageView>(R.id.profileImage)

        usernameTextView.text = username
        emailTextView.text = email
        contactTextView.text = contact
        cnicTextView.text = cnic

        // Load profile image from URL
        Glide.with(this)
            .asBitmap()
            .load(profileImageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Set rounded image
                    val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                    roundedDrawable.cornerRadius = 50f // Adjust as per your requirement
                    profileImageView.setImageDrawable(roundedDrawable)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Not implemented
                }


        findViewById<TextView>(R.id.usernameTextView).text = username
        findViewById<TextView>(R.id.emailTextView).text = email
        findViewById<TextView>(R.id.contactTextView).text = contact

        val profileImageUrl = sharedPreferences.getString("picture", "")
        profileImageUrl?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(profileImage)
        }
    }

    private fun setUserDetails(patient: Patient) {

        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show()
        val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val contactTextView = findViewById<TextView>(R.id.contactTextView)
        val cnicTextView = findViewById<TextView>(R.id.cnicTextView)
        val profileImageView = findViewById<ImageView>(R.id.profileImage)

        usernameTextView.text = patient.name
        emailTextView.text = patient.email
        contactTextView.text = patient.contactNumber
        cnicTextView.text = patient.cnic
        Toast.makeText(this, "Data Fetched", Toast.LENGTH_SHORT).show()
        // Save fetched user details to SharedPreferences

        findViewById<TextView>(R.id.usernameTextView).text = patient.name
        findViewById<TextView>(R.id.emailTextView).text = patient.email
        findViewById<TextView>(R.id.contactTextView).text = patient.contactNumber
        profileImage.setImageURI(Uri.parse(patient.picture))

        saveUserDetailsToSharedPreferences(patient)
    }

    private fun saveUserDetailsToSharedPreferences(patient: Patient) {
        val editor = sharedPreferences.edit()
        editor.putString("name", patient.name)
        editor.putString("email", patient.email)

        editor.putString("cnic", patient.cnic)

        editor.putString("picture", patient.picture)
        editor.apply()
    }

    private fun logoutUser() {

        // Update SharedPreferences to mark the user as logged out
        val editor = sharedPreferences2.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()

        startActivity(Intent(this, login::class.java))
        finish()
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            profileImage.setImageURI(imageUri)
            uploadImageToFirebaseStorage(imageUri!!)
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val fileReference = storageReference.child("${auth.currentUser?.uid}.jpg")
        fileReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    saveImageUrlToDatabase(imageUrl)
                }
            }
            .addOnFailureListener { e ->
                // Handle failed upload
            }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        databaseReference.child("picture").setValue(imageUrl)
            .addOnSuccessListener {
                val editor = sharedPreferences.edit()
                editor.putString("picture", imageUrl)
                editor.apply()
            }
            .addOnFailureListener { e ->
                // Handle failed database operation
            }
    }
}
