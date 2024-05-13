package com.ahmadmustafa.docconnect

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class viewCenter : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: viewPagerViewCenterAdapter
    private lateinit var centerName: String
    private lateinit var centerImage: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var feedbackImageView: ImageView
    private lateinit var ratingTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_center)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        adapter = viewPagerViewCenterAdapter(this)
        viewPager.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })

        // Retrieve centerName from intent extras
        centerName = intent.getStringExtra("centerName").toString()

        // Initialize UI elements
        centerImage = findViewById(R.id.profileImage)
        nameTextView = findViewById(R.id.nameTextView)
        categoryTextView = findViewById(R.id.categoryTextView)
        locationTextView = findViewById(R.id.locationTextView)
        feedbackImageView = findViewById(R.id.feedback)
        ratingTextView = findViewById(R.id.rating)

        // Retrieve center details based on centerName
        fetchCenterDetails()
    }

    private fun fetchCenterDetails() {
        val database = FirebaseDatabase.getInstance().reference.child("centers")
        database.orderByChild("name").equalTo(centerName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Center found, populate UI with center details
                    val center = dataSnapshot.children.first().getValue(Center::class.java)
                    center?.let {
                        // Load image using Glide
                        if (!center.picture.isNullOrEmpty()) {
                            Glide.with(centerImage.context)
                                .load(center.picture)
                                .into(centerImage)
                        } else {
                            // Handle case where picture is null or empty
                        }

                        nameTextView.text = center.name
                        categoryTextView.text = center.category
                        locationTextView.text = center.address
                    }
                } else {
                    // Center not found, handle this case if needed
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
}
