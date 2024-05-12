package com.ahmadmustafa.docconnect
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
class chatBox : AppCompatActivity() {

    private lateinit var receiverTextView: TextView
    private lateinit var profileImage: de.hdodenhof.circleimageview.CircleImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chats: MutableList<Chat>
    private lateinit var sendButton: ImageButton
    private lateinit var attachButton: ImageButton
    private lateinit var mediaUri: Uri

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            mediaUri = it
            // Handle media upload
            uploadMedia()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_box)

        receiverTextView = findViewById(R.id.recieverTextView)
        profileImage = findViewById(R.id.profileImage)
        recyclerView = findViewById(R.id.userRecyclerView)
        sendButton = findViewById(R.id.sendButton)
        attachButton = findViewById(R.id.addButton)

        val professionalId = intent.getStringExtra("professionalId")
        val senderId = intent.getStringExtra("senderId").toString()
        val recieverId = intent.getStringExtra("recieverId").toString()
        if (professionalId != null) {
            fetchProfessionalData(professionalId)
        }

        chats = mutableListOf()
        chatAdapter = ChatAdapter(chats, senderId )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter
        val messageEditText = findViewById<EditText>(R.id.messageEditText)
        sendButton.setOnClickListener {
            val message = messageEditText.text.toString()
            sendMessage(message)
        }


        attachButton.setOnClickListener {
            // Open gallery for media selection
            getContent.launch("image/*") // Assuming getContent is a registered activity result launcher for selecting media
        }
    }

    private fun fetchProfessionalData(professionalId: String) {
        val professionalRef = FirebaseDatabase.getInstance().getReference("professionals").child(professionalId)
        professionalRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val professional = dataSnapshot.getValue(Professional::class.java)
                    professional?.let {
                        receiverTextView.text = professional.name
                        Glide.with(this@chatBox)
                            .load(professional.picture)
                            .into(profileImage)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun sendMessage(message: String) {
        val chatRef = FirebaseDatabase.getInstance().getReference("chats")
        val chatId = chatRef.push().key ?: ""
        val currentTime = System.currentTimeMillis()
        val chat = Chat("senderId", "receiverId", message, currentTime)
        chatRef.child(chatId).setValue(chat)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun uploadMedia() {
        val storageRef = FirebaseStorage.getInstance().reference
        val mediaRef = storageRef.child("media").child(mediaUri.lastPathSegment ?: "")
        mediaRef.putFile(mediaUri)
            .addOnSuccessListener {
                // Handle media upload success
                // Get the media download URL and send it along with the message
                mediaRef.downloadUrl.addOnSuccessListener { uri ->
                    val message = "" // Optionally, you can include a message with the media
                    sendMessage(message)
                }.addOnFailureListener {
                    // Handle failure to get media download URL
                }
            }
            .addOnFailureListener {
                // Handle media upload failure
            }
    }
}
