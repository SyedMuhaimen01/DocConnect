package com.ahmadmustafa.docconnect
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage

class editDoctorProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var nameTextView: TextView
    private lateinit var profileImageView: com.google.android.material.imageview.ShapeableImageView
    private var imageUri: Uri? = null
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImage ->
            uploadImageToFirebaseStorage(selectedImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_doctor_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = Firebase.storage.reference

        nameTextView = findViewById(R.id.username)
        profileImageView = findViewById(R.id.profileImage)
        val editProfileImageButton = findViewById<ImageView>(R.id.profileImage)

        editProfileImageButton.setOnClickListener {
            openGalleryForImage()
        }

        // Fetch and display professional's name and profile picture
        displayProfessionalProfile()
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
                    database.child("professionals").child(auth.currentUser?.uid!!).child("picture").setValue(imageUrl)
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

    private fun displayProfessionalProfile() {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            val professionalRef = database.child("professionals").child(uid)
            professionalRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val imageUrl = snapshot.child("picture").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val contactInfo = snapshot.child("contactNumber").getValue(String::class.java)
                    val specialization = snapshot.child("specialization").getValue(String::class.java)
                    val affiliation = snapshot.child("affiliation").getValue(String::class.java)
                    val password = snapshot.child("password").getValue(String::class.java)

                    name?.let { findViewById<EditText>(R.id.username)?.setText(it) }
                    email?.let { findViewById<EditText>(R.id.email)?.setText(it) }
                    contactInfo?.let { findViewById<EditText>(R.id.contact)?.setText(it) }
                    specialization?.let { findViewById<EditText>(R.id.specialization)?.setText(it) }
                    affiliation?.let { findViewById<EditText>(R.id.affiliation)?.setText(it) }
                    password?.let { findViewById<EditText>(R.id.passwordEditText)?.setText(it) }



                    imageUrl?.let { url ->
                        Glide.with(this@editDoctorProfile)
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

}
