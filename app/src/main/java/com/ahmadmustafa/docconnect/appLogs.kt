package com.ahmadmustafa.docconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

data class AppLog(
    val id: String = "",
    val title: String = "",
    val notification: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", 0)
}

class appLogs : AppCompatActivity() {

    private lateinit var appLogsAdapter: AppLogsAdapter
    private val appLogsList = mutableListOf<AppLog>()
    private lateinit var auth: FirebaseAuth
    private lateinit var logsReference: DatabaseReference
    private lateinit var centerReference: DatabaseReference
    private lateinit var professionalReference: DatabaseReference
    private lateinit var patientReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_logs)

        // Initialize the RecyclerView and its adapter
        val logsRecyclerView = findViewById<RecyclerView>(R.id.logsRecyclerView)
        appLogsAdapter = AppLogsAdapter(appLogsList)
        logsRecyclerView.adapter = appLogsAdapter
        logsRecyclerView.layoutManager = LinearLayoutManager(this)
        auth = FirebaseAuth.getInstance()

        // Initialize the Firebase Database reference
        logsReference = FirebaseDatabase.getInstance().getReference("logs")
        centerReference = FirebaseDatabase.getInstance().getReference("centers")
        professionalReference = FirebaseDatabase.getInstance().getReference("professionals")
        patientReference= FirebaseDatabase.getInstance().getReference("patients")

        // Call the function to set up the Firebase listener
        setupFirebaseListeners()
        setupFirebaseListeners2()
        // Log message to indicate that the activity is created

    }

    private fun setupFirebaseListeners() {
        // Listen for new logs added to the database
        logsReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val log = snapshot.getValue(AppLog::class.java)
                log?.let {
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle log changes if needed
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle log removal if needed
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle log movement if needed
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun setupFirebaseListeners2() {
        // Listen for changes in the database
        centerReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is triggered once when the listener is attached
                // and again every time the data at this location is updated.
                // You can handle data changes here.
                for (childSnapshot in snapshot.children) {
                    val log = childSnapshot.getValue(AppLog::class.java)
                    log?.let { addLog(it)
                        addLogToFirebase("center Activity", "Signup")}
                }
                // You can also add your log here if needed

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }


    private fun addLog(log: AppLog) {
        // Check if a similar log already exists in the local list
        val existingLog = appLogsList.find { it.id == log.id }
        if (existingLog == null) {
            // Add the log to the local list
            appLogsList.add(log)
            appLogsAdapter.notifyItemInserted(appLogsList.size - 1)
        }
    }

    // Function to add a log to Firebase
    private fun addLogToFirebase(title: String, notification: String) {
        val logId = UUID.randomUUID().toString() // Generate a unique ID for the log
        val log = AppLog(logId, title, notification)
        logsReference.child(logId).setValue(log)
            .addOnSuccessListener {
                // Log message to indicate that log is successfully added to Firebase
                println("Log added to Firebase: $title - $notification")
            }
            .addOnFailureListener { e ->
                // Log message if adding log to Firebase fails
                println("Failed to add log to Firebase: ${e.message}")
            }
    }

    // Example function to log something
    private fun logSomething() {
        val title = "Title"
        val notification = "Notification"
        addLogToFirebase(title, notification)
    }
}
