package com.kilica.bitirmeproje.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.kilica.bitirmeproje.R

class IlanDetay : AppCompatActivity() {
    private lateinit var sahaTextView: TextView
    private lateinit var saatTextView: TextView
    private lateinit var mevkiTextView: TextView
    private lateinit var notTextView: TextView
    private lateinit var profileButton: Button
    private lateinit var firestore: FirebaseFirestore
    private var ilanId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ilan_detay)

        sahaTextView = findViewById(R.id.textViewSaha)
        saatTextView = findViewById(R.id.textViewSaat)
        mevkiTextView = findViewById(R.id.textViewMevki)
        notTextView = findViewById(R.id.textViewNot)
        profileButton = findViewById(R.id.buttonProfile)
        firestore = FirebaseFirestore.getInstance()

        ilanId = intent.getStringExtra("ilanId")
        ilanId?.let {
            loadIlanDetay(it)
        }

        profileButton.setOnClickListener {
            ilanId?.let {
                firestore.collection("ilanlar").document(it)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val userId = document.getString("userId")
                            if (userId != null) {
                                val intent = Intent(this, UserProfile::class.java)
                                intent.putExtra("userId", userId)
                                startActivity(intent)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Kullanıcı bilgisi alınamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadIlanDetay(ilanId: String) {
        firestore.collection("ilanlar").document(ilanId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    sahaTextView.text = document.getString("saha")
                    saatTextView.text = document.getString("saat")
                    mevkiTextView.text = document.getString("mevki")
                    notTextView.text = document.getString("not")
                } else {
                    Toast.makeText(this, "İlan bulunamadı.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "İlan detayları yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}