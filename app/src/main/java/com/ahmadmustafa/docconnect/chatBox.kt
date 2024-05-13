package com.ahmadmustafa.docconnect

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class chatBox : AppCompatActivity() {

    private lateinit var currentUserUid: String
    private lateinit var receiverId: String
    private lateinit var receiverName: String
    private lateinit var receiverProfileImageUrl: String
    private lateinit var chatAdapter: ChatAdapter
    private val chats: MutableList<Chat> = mutableListOf()

    private lateinit var storageRef: StorageReference
    private lateinit var reference: DatabaseReference
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Handle the selected image or video URI here
                uploadMediaToFirebase(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_box)

        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        receiverId = intent.getStringExtra("receiverId").toString()
        receiverName = intent.getStringExtra("receiverName").toString()

        if (receiverName.isNotEmpty()) {
            getUserType(receiverName)
        } else {
            Toast.makeText(this, "Name is empty", Toast.LENGTH_SHORT).show()
        }

        val receiverTextView: TextView = findViewById(R.id.receiverTextView)
        receiverTextView.text = receiverName

        storageRef = FirebaseStorage.getInstance().reference
        reference = FirebaseDatabase.getInstance().getReference("chats")

        val recyclerView: RecyclerView = findViewById(R.id.userRecyclerView)
        chatAdapter = ChatAdapter(chats, currentUserUid)
        recyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@chatBox)
        }

        // Set up onClickListener for send button...
        val sendButton: ImageButton = findViewById(R.id.sendButton)
        val messageEditText: EditText = findViewById(R.id.messageEditText)
        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message, null)
                messageEditText.text.clear()
            }
        }

        // Set up onClickListener for add button...
        val addButton: ImageButton = findViewById(R.id.addButton)
        addButton.setOnClickListener {
            // Open gallery to select image or video
            getContent.launch("image/*")
            getContent.launch("video/*")

        }
    }

    private fun uploadMediaToFirebase(uri: Uri) {
        val fileReference = storageRef.child("media/${UUID.randomUUID()}")

        fileReference.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    // Handle successful upload, get the download URL and send it in a message
                    sendMessage("", downloadUri.toString())
                }
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful uploads
            }
    }

    private fun sendMessage(message: String, mediaUrl: String?) {
        val chatRef = FirebaseDatabase.getInstance().getReference("chats")
        val chatId = chatRef.push().key ?: ""
        val currentTime = System.currentTimeMillis()
        val chat = if (mediaUrl != null) {
            Chat(currentUserUid, receiverId, message, currentTime, Chat.ContentType.IMAGE, mediaUrl)
        } else {
            Chat(currentUserUid, receiverId, message, currentTime, Chat.ContentType.TEXT, null)
        }

        chatRef.child(chatId).setValue(chat)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun readMessage(senderId: String, receiverId: String) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chats.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(Chat::class.java)
                    if ((chat?.senderId == senderId && chat.receiverId == receiverId) ||
                        (chat?.senderId == receiverId && chat.receiverId == senderId)) {
                        chat?.let { chats.add(it) }
                    }
                }
                chatAdapter.notifyDataSetChanged()
                if (chats.isNotEmpty()) {
                    // Assuming recyclerView is defined in the scope of the class
                    val recyclerView: RecyclerView = findViewById(R.id.userRecyclerView)
                    recyclerView.scrollToPosition(chats.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun getUserType(receiverName: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            findPatient(userId) { isPatient ->
                if (isPatient) {
                    findProfessionalByName(receiverName)
                    // User is a patient
                    // Handle patient logic here
                } else {
                    findProfessional(userId) { isProfessional ->
                        if (isProfessional) {
                            findPatientByName(receiverName)
                            // Handle professional logic here
                        } else {
                            // Handle the case where user is neither a patient nor a professional
                        }
                    }
                }
            }
        } ?: Log.e("FetchPatientData", "Current user is null")
    }

    private fun findPatient(userId: String, callback: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("patients")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val id = userSnapshot.child("id").getValue(String::class.java)
                    Log.d("FindPatient", "Checking user with ID: $id")
                    if (id == userId) {
                        Log.d("FindPatient", "Patient found with ID: $userId")
                        callback(true)
                        return
                    }
                }
                // Handle the case where user with the given ID is not found
                Log.d("FindPatient", "User with ID $userId is not a patient")
                Toast.makeText(applicationContext, "User with ID $userId is not a patient", Toast.LENGTH_SHORT).show()
                callback(false)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
                Log.e("FindPatient", "Database error: ${error.message}")
                callback(false)
            }
        })
    }

    private fun findProfessional(userId: String, callback: (Boolean) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("professionals")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val id = userSnapshot.child("id").getValue(String::class.java)
                    Log.d("FindProfessional", "Checking user with ID: $id")
                    if (id == userId) {
                        Log.d("FindProfessional", "Professional found with ID: $userId")
                        callback(true)
                        return
                    }
                }
                // Handle the case where user with the given ID is not found
                Log.d("FindProfessional", "User with ID $userId is not a professional")
                Toast.makeText(applicationContext, "User with ID $userId is not a professional", Toast.LENGTH_SHORT).show()
                callback(false)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
                Log.e("FindProfessional", "Database error: ${error.message}")
                callback(false)
            }
        })
    }

    private fun findProfessionalByName(userName: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference("professionals")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val name = userSnapshot.child("name").getValue(String::class.java)
                    Log.d("FindProfessionalByName", "Checking user with name: $name")
                    if (name == userName) {
                        receiverId = userSnapshot.key.toString() // Set receiver ID
                        receiverName = name // Set receiver name
                        Log.d("FindProfessionalByName", "Professional found with name $userName and ID: $receiverId")
                        readMessage(currentUserUid, receiverId)
                        // Load profile image into ImageView using Glide
                        val profileImage: ImageView = findViewById(R.id.profileImage)
                        val imageUrl = userSnapshot.child("picture").getValue(String::class.java)
                        imageUrl?.let { url ->
                            Glide.with(this@chatBox).load(url).centerCrop().into(profileImage)
                        }
                        return // Exit the loop once the user is found
                    }
                }
                // Handle the case where user with the given name is not found
                Log.d("FindProfessionalByName", "Professional with name $userName not found")
                Toast.makeText(applicationContext, "Professional with name $userName not found", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
                Log.e("FindProfessionalByName", "Database error: ${error.message}")
            }
        })
    }

    private fun findPatientByName(userName: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference("patients")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val name = userSnapshot.child("name").getValue(String::class.java)
                    Log.d("FindPatientByName", "Checking user with name: $name")
                    if (name == userName) {
                        receiverId = userSnapshot.key.toString() // Set receiver ID
                        receiverName = name // Set receiver name
                        Log.d("FindPatientByName", "Patient found with name $userName and ID: $receiverId")
                        readMessage(currentUserUid, receiverId)
                        // Load profile image into ImageView using Glide
                        val profileImage: ImageView = findViewById(R.id.profileImage)
                        val imageUrl = userSnapshot.child("picture").getValue(String::class.java)
                        imageUrl?.let { url ->
                            Glide.with(this@chatBox).load(url).centerCrop().into(profileImage)
                        }
                        return // Exit the loop once the user is found
                    }
                }
                // Handle the case where user with the given name is not found
                Log.d("FindPatientByName", "Patient with name $userName not found")
                Toast.makeText(applicationContext, "Patient with name $userName not found", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
                Log.e("FindPatientByName", "Database error: ${error.message}")
            }
        })
    }


}
