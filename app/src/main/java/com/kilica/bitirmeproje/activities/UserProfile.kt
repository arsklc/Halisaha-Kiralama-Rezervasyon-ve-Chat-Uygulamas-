package com.kilica.bitirmeproje.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kilica.bitirmeproje.R

class UserProfile : AppCompatActivity() {
    private lateinit var userNameTextView: TextView
    private lateinit var userSurnameTextView: TextView
    private lateinit var userAgeTextView: TextView
    private lateinit var userHeightTextView: TextView
    private lateinit var userWeightTextView: TextView
    private lateinit var userCityTextView: TextView
    private lateinit var mediaLinearLayout: LinearLayout
    private lateinit var messageButton: Button
    private lateinit var firestore: FirebaseFirestore
    private var userId: String? = null
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        userNameTextView = findViewById(R.id.textViewUserName)
        userSurnameTextView = findViewById(R.id.textViewUserSurname)
        userAgeTextView = findViewById(R.id.textViewUserAge)
        userHeightTextView = findViewById(R.id.textViewUserHeight)
        userWeightTextView = findViewById(R.id.textViewUserWeight)
        userCityTextView = findViewById(R.id.textViewUserCity)
        mediaLinearLayout = findViewById(R.id.mediaLinearLayout)
        messageButton = findViewById(R.id.buttonMessage)
        firestore = FirebaseFirestore.getInstance()

        userId = intent.getStringExtra("userId")
        userId?.let {
            loadUserProfile(it)
        }

        messageButton.setOnClickListener {
            userId?.let {
                val chatId = if (currentUser?.uid!! < it) {
                    "${currentUser.uid}_$it"
                } else {
                    "${it}_${currentUser.uid}"
                }
                val intent = Intent(this, Chat::class.java)
                intent.putExtra("chatId", chatId)
                intent.putExtra("receiverId", it)
                startActivity(intent)
            }
        }
    }

    private fun loadUserProfile(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    userNameTextView.text = document.getString("firstName")
                    userSurnameTextView.text = document.getString("lastName")
                    userAgeTextView.text = document.getString("age")
                    userHeightTextView.text = document.getString("height")
                    userWeightTextView.text = document.getString("weight")
                    userCityTextView.text = document.getString("city")

                    val mediaUrls = document.get("media") as? List<String>
                    if (mediaUrls != null) {
                        mediaLinearLayout.removeAllViews()

                        val displayMetrics = DisplayMetrics()
                        windowManager.defaultDisplay.getMetrics(displayMetrics)
                        val screenHeight = displayMetrics.heightPixels
                        val screenWidth = displayMetrics.widthPixels

                        val imageHeight = screenHeight / 3
                        val imageWidth = screenWidth / 2

                        mediaUrls.forEach { url ->
                            val uri = Uri.parse(url)
                            val imageView = ImageView(this).apply {
                                layoutParams = LinearLayout.LayoutParams(imageWidth, imageHeight).apply {
                                    marginEnd = 16
                                }
                            }
                            Glide.with(this).load(uri).into(imageView)
                            mediaLinearLayout.addView(imageView)
                        }
                    }
                } else {
                    Toast.makeText(this, "Kullanıcı bilgileri bulunamadı.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Kullanıcı bilgileri yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}