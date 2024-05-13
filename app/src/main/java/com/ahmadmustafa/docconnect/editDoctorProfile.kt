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
import android.widget.TextView
import android.widget.Toast
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

    private lateinit var sharedPreferences: SharedPreferences
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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_doctor_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = Firebase.storage.reference
        sharedPreferences = getSharedPreferences("professionals", Context.MODE_PRIVATE)
        nameTextView = findViewById(R.id.username)
        profileImageView = findViewById(R.id.profileImage)
        val editProfileImageButton = findViewById<ImageView>(R.id.profileImage)

        editProfileImageButton.setOnClickListener {
            openGalleryForImage()
        }

        // Fetch and display professional's name and profile picture
        displayProfessionalProfile()
        val editProfileButton = findViewById<Button>(R.id.editprofile)
        editProfileButton.setOnClickListener {
            updateProfileData()
        }
        val homeButton= findViewById<ImageButton>(R.id.home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, doctorViewAppointmentsList::class.java))
        }

        val chatButton= findViewById<ImageButton>(R.id.chat)
        chatButton.setOnClickListener {
            startActivity(Intent(this, chatBox::class.java).apply {
                putExtra("userType", "professional")
            })
        }
        val workingHoursButton= findViewById<ImageButton>(R.id.workingHours)
        workingHoursButton.setOnClickListener {
            startActivity(Intent(this, setWorkingHours::class.java).apply {
                putExtra("userType", "professional")
            })
        }
        val profileButton= findViewById<ImageButton>(R.id.profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, doctorProfile::class.java))
        }
        val appointButton= findViewById<ImageButton>(R.id.appoint)
        appointButton.setOnClickListener {
            startActivity(Intent(this, doctorViewAppointmentsList::class.java).apply {
                putExtra("userType", "professional")
            })
        }

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
                    password?.let { findViewById<EditText>(R.id.password)?.setText(it) }

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


    private fun updateProfileData() {
        val userId = auth.currentUser?.uid
        userId?.let { uid ->
            val professionalRef = database.child("professionals").child(uid)
            val newName = findViewById<EditText>(R.id.username).text.toString()
            val newEmail = findViewById<EditText>(R.id.email).text.toString()
            val newContactInfo = findViewById<EditText>(R.id.contact).text.toString()
            val newSpecialization = findViewById<EditText>(R.id.specialization).text.toString()
            val newAffiliation = findViewById<EditText>(R.id.affiliation).text.toString()
            val newPassword = findViewById<EditText>(R.id.password).text.toString()

            professionalRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentName = snapshot.child("name").getValue(String::class.java)
                    val currentEmail = snapshot.child("email").getValue(String::class.java)
                    val currentContact = snapshot.child("contactNumber").getValue(String::class.java)
                    val currentSpecialization = snapshot.child("specialization").getValue(String::class.java)
                    val currentAffiliation = snapshot.child("affiliation").getValue(String::class.java)
                    val currentPassword = snapshot.child("password").getValue(String::class.java)

                    val updates = hashMapOf<String, Any>()


                        updates["name"] = newName


                        updates["email"] = newEmail


                        updates["contactNumber"] = newContactInfo


                        updates["specialization"] = newSpecialization


                        updates["affiliation"] = newAffiliation


                        updates["password"] = newPassword


                    professionalRef.updateChildren(updates)
                        .addOnSuccessListener {
                            // Update successful in Firebase
                            updateSharedPreferencesIfChanged(
                                newName,
                                newEmail,
                                newContactInfo,
                                newSpecialization,
                                newAffiliation,
                                newPassword,
                                currentName,
                                currentEmail,
                                currentContact,
                                currentSpecialization,
                                currentAffiliation,
                                currentPassword
                            )
                            showToast("Profile updated successfully")
                        }
                        .addOnFailureListener { e ->
                            // Handle failure
                            showToast("Failed to update profile")
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled event
                }
            })
        }
    }



    private fun updateSharedPreferencesIfChanged(
        newName: String?,
        newEmail: String?,
        newContact: String?,
        newSpecialization: String?,
        newAffiliation: String?,
        newPassword: String?,
        currentName: String?,
        currentEmail: String?,
        currentContact: String?,
        currentSpecialization: String?,
        currentAffiliation: String?,
        currentPassword: String?
    ) {
        val editor = sharedPreferences.edit()

        // Compare each new value with the current value, and update SharedPreferences if changed
        if (newName != currentName) {
            editor.putString("name", newName)
        }
        if (newEmail != currentEmail) {
            editor.putString("email", newEmail)
        }
        if (newContact != currentContact) {
            editor.putString("contact", newContact)
        }
        if (newSpecialization != currentSpecialization) {
            editor.putString("specialization", newSpecialization)
        }
        if (newAffiliation != currentAffiliation) {
            editor.putString("affiliation", newAffiliation)
        }
        if (newPassword != currentPassword) {
            editor.putString("password", newPassword)
        }

        // Apply changes
        editor.apply()
    }



    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}

