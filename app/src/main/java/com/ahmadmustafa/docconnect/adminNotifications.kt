package com.ahmadmustafa.docconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.util.Date

data class Notification(
    val id: String, // Unique identifier for the notification
    val centerName: String, // Name of the center
    val timestamp: Date, // Timestamp when the notification was generated
    val title: String // Title of the notification
)


class adminNotifications : AppCompatActivity(), AdminNotificationsAdapter.OnNotificationClickListener {

    private lateinit var notificationsAdapter: AdminNotificationsAdapter
    private lateinit var notificationsRecyclerView: RecyclerView
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_notifications)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView)
        databaseReference = FirebaseDatabase.getInstance().getReference("centers")

        notificationsAdapter = AdminNotificationsAdapter(mutableListOf(), this)
        notificationsRecyclerView.adapter = notificationsAdapter
        notificationsRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchNotificationsFromFirebase()
    }

    private fun fetchNotificationsFromFirebase() {
        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val center = snapshot.getValue(Center::class.java)
                center?.let {
                    val notification = Notification(
                        id = snapshot.key!!,
                        centerName = center.name,
                        timestamp = Date(),
                        title = "Signup Request"
                    )
                    notificationsAdapter.addNotification(notification)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onNotificationClick(notification: Notification) {
        // Handle notification click event here
        // Redirect to addCenters page
        val intent = Intent(this, addCenters::class.java)
        startActivity(intent)
    }
}
