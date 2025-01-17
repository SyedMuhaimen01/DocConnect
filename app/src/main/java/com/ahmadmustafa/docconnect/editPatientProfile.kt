package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class editPatientProfile : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var profileImage: ShapeableImageView
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    @SuppressLint("MissingInflatedId")
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
            startActivity(Intent(this, Home::class.java).apply {
                putExtra("userType", "patient")
            })
        }

        val chatButton = findViewById<ImageButton>(R.id.chats)
        chatButton.setOnClickListener {
            startActivity(Intent(this, searchUsers::class.java).apply { putExtra("userType", "patient")})
        }

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }
        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            startActivity(Intent(this, map::class.java).apply { putExtra("userType", "patient")})
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, patientProfile::class.java))
        }

        val appointButton = findViewById<ImageButton>(R.id.appoint)
        appointButton.setOnClickListener {
            startActivity(Intent(this, manageAppointments::class.java).apply {
                putExtra("userType", "patient")
            })
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

        val radius = resources.getDimension(R.dimen.corner_radius) // Adjust the corner radius as needed
        profileImage.shapeAppearanceModel = profileImage.shapeAppearanceModel
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, radius)
            .build()
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
                    if (snapshot.exists()) {
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
                                .circleCrop() // Circle crop the image
                                .into(profileImage)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
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
        imageUri?.let { uri ->
            val fileReference = storageReference.child("${auth.currentUser?.uid}.jpg")
            fileReference.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Once the image is uploaded, get the download URL
                    fileReference.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Update the picture attribute with the download URL
                        patientUpdates["picture"] = downloadUri.toString()
                        // Update the database with all the patient updates
                        databaseReference.updateChildren(patientUpdates)
                            .addOnSuccessListener {
                                // Save user details to SharedPreferences
                                saveUserDetailsToSharedPreferences(username, email, contact)
                                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // If no image is selected, update the other patient details without the picture
        if (imageUri == null) {
            databaseReference.updateChildren(patientUpdates)
                .addOnSuccessListener {
                    // Save user details to SharedPreferences
                    saveUserDetailsToSharedPreferences(username, email, contact)
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
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
                    // Once the image is uploaded, load it into the ImageView
                    profileImage.setImageURI(imageUri)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
