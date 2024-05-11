package com.ahmadmustafa.docconnect

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class editPatientProfile : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var profileImage: ImageView
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_patient_profile)

        sharedPreferences = getSharedPreferences("patients", Context.MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("patients").child(auth.currentUser?.uid ?: "")
        storageReference = FirebaseStorage.getInstance().getReference("profile_images")

        profileImage = findViewById(R.id.profileImage)
        profileImage.setOnClickListener {
            openFileChooser()
        }

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        val chatButton = findViewById<ImageButton>(R.id.chats)
        chatButton.setOnClickListener {
            startActivity(Intent(this, chatBox::class.java))
        }

        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            startActivity(Intent(this, Map::class.java))
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, patientProfile::class.java))
        }

        val editProfileButton = findViewById<Button>(R.id.editprof)
        editProfileButton.setOnClickListener {
            updateProfile()
        }

        if (isConnected()) {
            fetchUserDetailsFromFirebase()
        } else {
            fetchUserDetailsFromSharedPreferences()
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
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

                    Log.d("EditPatientProfile", "Data loaded from Firebase")

                    username?.let { findViewById<EditText>(R.id.usernameEditText).setText(it) }
                    email?.let { findViewById<EditText>(R.id.emailEditText).setText(it) }
                    contact?.let { findViewById<EditText>(R.id.contactEditText).setText(it) }

                    imageUrl?.let { url ->
                        Glide.with(this@editPatientProfile)
                            .load(url)
                            .circleCrop()
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.e("EditPatientProfile", "Failed to load image from Firebase: $e")
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.d("EditPatientProfile", "Image loaded from Firebase successfully")
                                    return false
                                }
                            })
                            .into(findViewById(R.id.profileImage))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("EditPatientProfile", "Error fetching user profile from Firebase: ${error.message}")
                }
            })
        }
    }

    private fun fetchUserDetailsFromSharedPreferences() {
        val username = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "")
        val contact = sharedPreferences.getString("contactNumber", "")

        Log.d("EditPatientProfile", "Data loaded from SharedPreferences")

        findViewById<EditText>(R.id.usernameEditText).setText(username)
        findViewById<EditText>(R.id.emailEditText).setText(email)
        findViewById<EditText>(R.id.contactEditText).setText(contact)
    }

    private fun updateProfile() {
        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val contactEditText = findViewById<EditText>(R.id.contactEditText)

        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val contact = contactEditText.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val patientUpdates: MutableMap<String, Any> = HashMap()
        patientUpdates["name"] = username
        patientUpdates["email"] = email
        patientUpdates["contactNumber"] = contact

        // Add the update for the picture attribute
        imageUri?.let {
            patientUpdates["picture"] = it.toString()
        }

        databaseReference.updateChildren(patientUpdates)
            .addOnSuccessListener {
                saveUserDetailsToSharedPreferences(username, email, contact)
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        uploadImage()
    }

    private fun saveUserDetailsToSharedPreferences(name: String, email: String, contact: String) {
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("email", email)
        editor.putString("contactNumber", contact)
        editor.apply()
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
            uploadImage()
        }
    }

    private fun uploadImage() {
        if (imageUri != null) {
            val fileReference = storageReference.child("${auth.currentUser?.uid}.jpg")
            fileReference.putFile(imageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    // Now load the uploaded image into the ImageView
                    loadUploadedProfileImage()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadUploadedProfileImage() {
        storageReference.child("${auth.currentUser?.uid}.jpg").downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this@editPatientProfile)
                .load(uri)
                .circleCrop()
                .into(profileImage)
        }.addOnFailureListener { e ->
            Log.e("EditPatientProfile", "Failed to load uploaded image: ${e.message}")
        }
    }
}
