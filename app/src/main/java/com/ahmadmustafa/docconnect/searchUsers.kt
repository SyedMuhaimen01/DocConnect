package com.ahmadmustafa.docconnect

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
class Chat {
    var senderId: String = ""
    var receiverId: String = ""
    var message: String = ""
    var time: Long = 0
    var contentType: ContentType = ContentType.TEXT
    var contentUri: String? = null

    constructor()

    constructor(
        senderId: String,
        receiverId: String,
        message: String,
        time: Long,
        contentType: ContentType = ContentType.TEXT,
        contentUri: String? = null
    ) {
        this.senderId = senderId
        this.receiverId = receiverId
        this.message = message
        this.time = time
        this.contentType = contentType
        this.contentUri = contentUri
    }

    enum class ContentType {
        TEXT,
        IMAGE,
        VIDEO
    }
}



class searchUsers : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var professionalAdapter: ProfessionalAdapter
    private lateinit var chats: MutableList<Chat>
    private lateinit var searchAutoCompleteTextView: AutoCompleteTextView
    private lateinit var databaseReference: DatabaseReference

    private var professionalName: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search_users)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.userRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        chats = mutableListOf()
        professionalAdapter = ProfessionalAdapter(chats)
        recyclerView.adapter = professionalAdapter

        // Initialize AutoCompleteTextView
        searchAutoCompleteTextView = findViewById(R.id.searchAutoCompleteTextView)

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("chats")

        // Set up adapter for AutoCompleteTextView
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        searchAutoCompleteTextView.setAdapter(adapter)

        val searchButton: ImageButton = findViewById(R.id.searchButton)

// Set OnClickListener for the search button
        searchButton.setOnClickListener {
            // Get the entered professional name from the AutoCompleteTextView
            professionalName = searchAutoCompleteTextView.text.toString().trim()

            // If the professional name is empty, show a toast and return
            if (professionalName.isEmpty()) {
                Toast.makeText(this, "Please enter a professional's name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the entered professional name is in the list of chats
            val professionalChat = chats.find { chat ->
                chat.senderId == professionalName || chat.receiverId == professionalName
            }

            // If the professional chat is found, start chatbox activity with necessary data
            if (professionalChat != null) {
                startActivity(Intent(this, chatBox::class.java).apply {
                    putExtra("professionalId", professionalName)
                    putExtra("senderId", professionalChat.senderId)
                    putExtra("receiverId", professionalChat.receiverId)
                })
            } else {
                startActivity(Intent(this, chatBox::class.java).apply {
                    putExtra("professionalId", professionalName)
                    if (professionalChat != null) {
                        putExtra("senderId", professionalChat.senderId)
                    }
                    if (professionalChat != null) {
                        putExtra("receiverId", professionalChat.receiverId)
                    }
                }
                )}
        }

        // Listen for changes in the database to update suggestions
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear previous suggestions
                chats.clear()
                var professionalIndex = -1 // Initialize index

                // Iterate through chats and add them to the list
                for ((index, snapshot) in dataSnapshot.children.withIndex()) {
                    val chat = snapshot.getValue(Chat::class.java)
                    chat?.let {
                        chats.add(chat)
                        // Check if the entered professional name matches
                        if (chat.senderId == professionalName || chat.receiverId == professionalName) {
                            professionalIndex = index // Store the index of matched professional
                        }
                    }
                }

                // Notify adapter that data set has changed
                professionalAdapter.notifyDataSetChanged()

                // If the professional was found, move their card to the top
                if (professionalIndex != -1 )
                    recyclerView.scrollToPosition(professionalIndex)
                }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }

}

