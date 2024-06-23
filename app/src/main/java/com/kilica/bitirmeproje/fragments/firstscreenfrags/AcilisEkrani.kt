package com.kilica.bitirmeproje.fragments.firstscreenfrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.kilica.bitirmeproje.R


class AcilisEkrani : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_acilis_ekrani, container, false)
        val imageButton1 = view.findViewById<ImageButton>(R.id.kiralamaIcin)
        imageButton1.setOnClickListener {
            val nextFragment = kiraciGirisEkrani()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment)
                .addToBackStack(null)
                .commit()
        }

        val imageButton2 = view.findViewById<ImageButton>(R.id.sahipIcin)
        imageButton2.setOnClickListener {
            val anotherFragment = halisahaGirisEkrani()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, anotherFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

}