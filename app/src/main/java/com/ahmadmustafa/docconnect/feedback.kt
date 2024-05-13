package com.ahmadmustafa.docconnect

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

data class Reviews(
    val id: String = "",
    val ratedBy: String = "",
    val rated: String = "",
    val description: String = "",
    val rating: Float = 0f,
    val feedbackFor: String = ""
)

class feedback : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var ratedBy: String
    private lateinit var rated: String
    private lateinit var feedbackFor: String
    private lateinit var name: String
    private lateinit var profileImageUri: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Retrieve details from the intent extras
        rated = intent.getStringExtra("id") ?: ""
        name = intent.getStringExtra("Name") ?: ""
        feedbackFor = intent.getStringExtra("feedbackFor") ?: ""
        profileImageUri = intent.getStringExtra("ProfileImage") ?: ""
        ratedBy = auth.currentUser?.uid ?: ""

        // Set mentor name in the appropriate field
        findViewById<TextView>(R.id.nameTextView).text = name

        // Load mentor profile image
        val imageView = findViewById<ImageView>(R.id.profileImageView)
        val requestOptions = RequestOptions().transform(CircleCrop())
        if (profileImageUri.isNotEmpty()) {
            Glide.with(this)
                .load(Uri.parse(profileImageUri))
                .apply(requestOptions)
                .into(imageView)
        }

        // Back button click listener
        findViewById<ImageView>(R.id.back).setOnClickListener {
            onBackPressed()
        }

        // Submit button click listener
        findViewById<Button>(R.id.submit).setOnClickListener {
            submitFeedback()
        }

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        ratingBar.setOnRatingBarChangeListener { _, ratingValue, _ ->
            // Optionally, you can perform some action when rating changes
        }
    }

    private fun updateRatingInDatabase(entityType: String, entityId: String, newRating: Float) {
        val entityRef = databaseReference.child(entityType).child(entityId)
        entityRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get the previous rating from the dataSnapshot
                val previousRating = dataSnapshot.child("rating").getValue(Float::class.java) ?: 0.0f

                // Calculate the new aggregate rating
                val aggregateRating = (previousRating + newRating) / 2

                // Format the aggregate rating to one decimal place
                val formattedRating = String.format("%.1f", aggregateRating.toFloat())

                // Update the 'rating' attribute value in the database with the formatted aggregate rating
                entityRef.child("rating").setValue(formattedRating.toFloat())
                    .addOnSuccessListener {
                        // Rating updated successfully
                    }
                    .addOnFailureListener { e ->
                        // Failed to update rating
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled event
            }
        })
    }

    private fun submitFeedback() {
        val feedbackDescription = findViewById<EditText>(R.id.feedbackEditText).text.toString()
        val newRating = findViewById<RatingBar>(R.id.ratingBar).rating

        val feedbackId = databaseReference.child("reviews").push().key ?: ""
        val feedback = Reviews(feedbackId, ratedBy, rated, feedbackDescription, newRating, feedbackFor)

        databaseReference.child("reviews").child(feedbackId).setValue(feedback)
            .addOnSuccessListener {
                // Feedback saved successfully
                if (feedbackFor == "professional") {
                    updateRatingInDatabase("professionals", rated, newRating)
                } else {
                    updateRatingInDatabase("centers", rated, newRating)
                }
                onBackPressed()
            }
            .addOnFailureListener { e ->
                // Failed to save the feedback
                // You can handle the error here
            }
    }
}
