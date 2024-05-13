package com.ahmadmustafa.docconnect

import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Chat() {
    var senderId: String = ""
    var receiverId: String = ""
    var message: String? = null
    var time: Long? = null
    var contentType: ContentType? = null
    var contentUri: String? = null

    enum class ContentType {
        TEXT, IMAGE, VIDEO
    }

    constructor(
        senderId: String,
        receiverId: String,
        message: String?,
        time: Long?,
        contentType: ContentType?,
        contentUri: String?
    ) : this() {
        this.senderId = senderId
        this.receiverId = receiverId
        this.message = message
        this.time = time
        this.contentType = contentType
        this.contentUri = contentUri
    }
}

class searchUsers : AppCompatActivity() {

    private lateinit var searchAutoCompleteTextView: AutoCompleteTextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var professionalAdapter: professionalAdapter
    private val chats = mutableListOf<Chat>()
    private lateinit var currentUserUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_users)

        searchAutoCompleteTextView = findViewById(R.id.searchAutoCompleteTextView)
        val searchButton: ImageButton = findViewById(R.id.searchButton)
        recyclerView = findViewById(R.id.userRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        professionalAdapter = professionalAdapter(chats) { chat ->
            val professionalName = searchAutoCompleteTextView.text.toString().trim()
            startActivity(Intent(this, chatBox::class.java).apply {
                putExtra("receiverName", professionalName)
            })
        }
        recyclerView.adapter = professionalAdapter

        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        searchButton.setOnClickListener {
            val professionalName = searchAutoCompleteTextView.text.toString().trim()

            if (professionalName.isEmpty()) {
                Toast.makeText(this, "Please enter a professional's name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startActivity(Intent(this, chatBox::class.java).apply {
                putExtra("receiverName", professionalName)
            })
        }

        // Listen for changes in the database to update suggestions
        val databaseReference = FirebaseDatabase.getInstance().getReference("chats")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chats.clear()
                dataSnapshot.children.forEach { snapshot ->
                    val chat = snapshot.getValue(Chat::class.java)
                    chat?.let {
                        val otherUserId = if (it.senderId == currentUserUid) it.receiverId else it.senderId
                        if (it.senderId == currentUserUid || it.receiverId == currentUserUid) {
                            if (!chats.any { chat -> chat.senderId == otherUserId || chat.receiverId == otherUserId }) {
                                chats.add(it)
                            }
                        }
                    }
                }
                professionalAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
}