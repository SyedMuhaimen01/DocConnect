package com.ahmadmustafa.docconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.util.*

data class AppLog(
    val title: String,
    val notification: String,
    val timestamp: Long = System.currentTimeMillis()
)

class appLogs : AppCompatActivity() {

    private lateinit var appLogsAdapter: AppLogsAdapter
    private val appLogsList = mutableListOf<AppLog>()
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_logs)

        // Initialize the RecyclerView and its adapter
        val logsRecyclerView = findViewById<RecyclerView>(R.id.logsRecyclerView)
        appLogsAdapter = AppLogsAdapter(appLogsList)
        logsRecyclerView.adapter = appLogsAdapter
        logsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        // Call the function to set up the Firebase listener
        setupFirebaseListeners()
    }

    private fun setupFirebaseListeners() {
        // Listen for changes in the 'patients' table
        val patientsReference = databaseReference.child("patients")
        patientsReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val patientId = snapshot.key
                val log = AppLog(
                    title = "Patient Signed Up",
                    notification = "New patient with ID $patientId signed up at ${Date()}"
                )
                addLog(log)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val patientId = snapshot.key
                val log = AppLog(
                    title = "Patient Profile Updated",
                    notification = "Patient with ID $patientId updated their profile at ${Date()}"
                )
                addLog(log)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            // Other overridden methods for ChildEventListener
        })

        // Listen for changes in the 'professionals' table
        val professionalsReference = databaseReference.child("professionals")
        professionalsReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val professionalId = snapshot.key
                val log = AppLog(
                    title = "Professional Signed Up",
                    notification = "New professional with ID $professionalId signed up at ${Date()}"
                )
                addLog(log)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val professionalId = snapshot.key
                val log = AppLog(
                    title = "Professional Profile Updated",
                    notification = "Professional with ID $professionalId updated their profile at ${Date()}"
                )
                addLog(log)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            // Other overridden methods for ChildEventListener
        })

        // Listen for changes in the 'centers' table
        val centersReference = databaseReference.child("centers")
        centersReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val centerId = snapshot.key
                val log = AppLog(
                    title = "Center Signed Up",
                    notification = "New center with ID $centerId signed up at ${Date()}"
                )
                addLog(log)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val centerId = snapshot.key
                val log = AppLog(
                    title = "Center Profile Updated",
                    notification = "Center with ID $centerId updated their profile at ${Date()}"
                )
                addLog(log)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            // Other overridden methods for ChildEventListener
        })
    }

    private fun addLog(log: AppLog) {
        appLogsList.add(log)
        appLogsAdapter.notifyItemInserted(appLogsList.size - 1)
    }
}
