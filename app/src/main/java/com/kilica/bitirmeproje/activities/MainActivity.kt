package com.kilica.bitirmeproje.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.databinding.ActivityMainBinding
import com.kilica.bitirmeproje.fragments.firstscreenfrags.AcilisEkrani

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        FirebaseApp.initializeApp(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container,AcilisEkrani())
                .commit()
        }
    }
}