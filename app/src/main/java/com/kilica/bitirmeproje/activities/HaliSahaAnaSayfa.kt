package com.kilica.bitirmeproje.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.fragments.halisahascreenfrags.DetayEkrani
import com.kilica.bitirmeproje.fragments.halisahascreenfrags.MesajEkrani
import com.kilica.bitirmeproje.fragments.halisahascreenfrags.RandevuEkrani

class HaliSahaAnaSayfa : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hali_saha_ana_sayfa)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_detay -> {
                    openFragment(DetayEkrani())
                    true
                }
                R.id.navigation_randevu -> {
                    openFragment(RandevuEkrani())
                    true
                }
                R.id.navigation_mesaj -> {
                    openFragment(MesajEkrani())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            openFragment(DetayEkrani())
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}