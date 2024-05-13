package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class editCenterProfile : AppCompatActivity() {

    private lateinit var sharedPreferences:SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var nameTextView: EditText
    private lateinit var profileImageView: com.google.android.material.imageview.ShapeableImageView
    private lateinit var emailEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var categoryEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var editButton: Button
    private var imageUri: Uri? = null
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImage ->
            uploadImageToFirebaseStorage(selectedImage)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_center_profile)
        sharedPreferences = getSharedPreferences("centers", Context.MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference

        nameTextView = findViewById(R.id.username)
        profileImageView = findViewById(R.id.profileImage)
        emailEditText = findViewById(R.id.email)
        contactEditText = findViewById(R.id.contact)
        addressEditText = findViewById(R.id.address)
        categoryEditText = findViewById(R.id.category)
        passwordEditText = findViewById(R.id.password)
        editButton = findViewById(R.id.editprofile)
        editButton.setOnClickListener {
            updateCenterProfile()
        }

        val editProfileImageButton = findViewById<ImageView>(R.id.profileImage)

        editProfileImageButton.setOnClickListener {
            openGalleryForImage()
        }

        val homeButton = findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, centreHome::class.java))
        }

        val chatButton = findViewById<ImageButton>(R.id.chats)
        chatButton.setOnClickListener {
            startActivity(Intent(this, chatBox::class.java).apply {
                putExtra("userType", "center")
            })
        }

        val mapButton = findViewById<ImageButton>(R.id.map)
        mapButton.setOnClickListener {
            startActivity(Intent(this, map::class.java).apply {
                putExtra("userType", "center")
            })
        }

        val profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, centerProfile::class.java))
        }

        // Fetch and display center's details
        displayCenterDetails()
    }

    private fun openGalleryForImage() {
        getContent.launch("image/*")
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val fileReference = storage.child("profile_images/${auth.currentUser?.uid}")
        fileReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    database.child("centers").child(auth.currentUser?.uid!!).child("picture").setValue(imageUrl)
                        .addOnSuccessListener {
                            // Image uploaded successfully
                            // You can show a toast or perform any other action
                        }
                        .addOnFailureListener { e ->
                            // Handle failure
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

    private fun displayCenterDetails() {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            val centerRef = database.child("centers").child(uid)
            centerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val imageUrl = snapshot.child("picture").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val contact = snapshot.child("contactNumber").getValue(String::class.java)
                    val address = snapshot.child("address").getValue(String::class.java)
                    val category = snapshot.child("category").getValue(String::class.java)
                    val password = snapshot.child("password").getValue(String::class.java)

                    name?.let { nameTextView.setText(it) }
                    email?.let { findViewById<EditText>(R.id.email)?.setText(it) }
                    contact?.let { findViewById<EditText>(R.id.contact)?.setText(it) }
                    address?.let { findViewById<EditText>(R.id.address)?.setText(it) }
                    category?.let { findViewById<EditText>(R.id.category)?.setText(it) }
                    password?.let { findViewById<EditText>(R.id.password)?.setText(it) }

                    imageUrl?.let { url ->
                        Glide.with(this@editCenterProfile)
                            .load(url)
                            .circleCrop()
                            .into(profileImageView)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled event
                }
            })
        }
    }

    private fun updateCenterProfile() {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            val centerRef = database.child("centers").child(uid)
            val newName = nameTextView.text.toString()
            val newEmail = emailEditText.text.toString()
            val newContactInfo = contactEditText.text.toString()
            val newAddress = addressEditText.text.toString()
            val newCategory = categoryEditText.text.toString()
            val newPassword = passwordEditText.text.toString()

            val updates = hashMapOf<String, Any>(
                "name" to newName,
                "email" to newEmail,
                "contactNumber" to newContactInfo,
                "address" to newAddress,
                "category" to newCategory,
                "password" to newPassword
            )

            centerRef.updateChildren(updates)
                .addOnSuccessListener {
                    saveCenterToSharedPreferences(
                        Center(
                            uid,
                            newName,
                            newEmail,
                            newContactInfo,
                            newAddress,
                            newCategory,
                            newPassword,
                            centerStatus = true, // Assuming it's always true upon update
                            "", // Picture will be updated separately
                            "" // Certificate will be updated separately
                        )
                    )
                    showToast("Profile updated successfully")
                }
                .addOnFailureListener { e ->
                    showToast("Failed to update profile")
                }
        }
    }

    private fun saveCenterToSharedPreferences(center: Center) {
        val editor = sharedPreferences.edit()
        editor.putString("centerId", center.id)
        editor.putString("name", center.name)
        editor.putString("email", center.email)
        editor.putString("contact", center.contactNumber)
        editor.putString("address", center.address)
        editor.putString("category", center.category)
        editor.putString("password", center.password)
        editor.putBoolean("status", center.centerStatus)
        editor.putString("picture", center.picture)
        editor.putString("certificate", center.certificate)
        editor.apply()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
