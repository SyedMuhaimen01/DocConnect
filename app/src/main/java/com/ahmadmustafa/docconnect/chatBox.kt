package com.ahmadmustafa.docconnect

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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

        val receiverTextView: TextView = findViewById(R.id.recieverTextView)
        receiverTextView.text = receiverName

        // Get receiver's profile image URL from Firebase
        FirebaseDatabase.getInstance().getReference("patients").child(receiverId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val patient = dataSnapshot.getValue(Patient::class.java)
                    patient?.let {
                        receiverProfileImageUrl = it.picture.toString()
                        // Load profile image into ImageView using Glide
                        val profileImage: ImageView = findViewById(R.id.profileImage)
                        Glide.with(this@chatBox).load(receiverProfileImageUrl).centerCrop()
                            .into(profileImage)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })

        storageRef = FirebaseStorage.getInstance().reference
        reference = FirebaseDatabase.getInstance().getReference("chats")

        val recyclerView: RecyclerView = findViewById(R.id.userRecyclerView)
        chatAdapter = ChatAdapter(chats, currentUserUid)
        recyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@chatBox)
        }

        readMessages()

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

    private fun readMessages() {
        val chatRef = FirebaseDatabase.getInstance().getReference("chats")
        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chats.clear()
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    chat?.let {
                        if ((chat.senderId == currentUserUid && chat.receiverId == receiverId) ||
                            (chat.senderId == receiverId && chat.receiverId == currentUserUid)) {
                            chats.add(chat)
                        }
                    }
                }
                chatAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }
}
