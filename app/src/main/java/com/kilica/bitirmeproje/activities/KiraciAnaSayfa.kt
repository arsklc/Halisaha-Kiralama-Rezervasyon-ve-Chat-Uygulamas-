package com.kilica.bitirmeproje.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.fragments.kiraciscreenfrags.AramaEkrani
import com.kilica.bitirmeproje.fragments.kiraciscreenfrags.IlanEkrani
import com.kilica.bitirmeproje.fragments.kiraciscreenfrags.MesajlasmaEkrani
import com.kilica.bitirmeproje.fragments.kiraciscreenfrags.ProfilEkrani

class KiraciAnaSayfa : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kiraci_ana_sayfa)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profil -> {
                    loadFragment(ProfilEkrani())
                    true
                }
                R.id.navigation_arama -> {
                    loadFragment(AramaEkrani())
                    true
                }
                R.id.navigation_ilan -> {
                    loadFragment(IlanEkrani())
                    true
                }
                R.id.navigation_mesajlasma -> {
                    loadFragment(MesajlasmaEkrani())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_profil
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}