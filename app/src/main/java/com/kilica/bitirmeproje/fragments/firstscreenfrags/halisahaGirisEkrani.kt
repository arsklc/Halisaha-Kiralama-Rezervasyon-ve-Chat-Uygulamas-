package com.kilica.bitirmeproje.fragments.firstscreenfrags

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.activities.HaliSahaAnaSayfa


class halisahaGirisEkrani : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_halisaha_giris_ekrani, container, false)

        emailEditText = view.findViewById(R.id.sahipMail)
        passwordEditText = view.findViewById(R.id.sahipSifre)

        val loginButton = view.findViewById<Button>(R.id.sahipGirisYap)
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(requireContext(), "Email ve şifre boş bırakılamaz", Toast.LENGTH_SHORT).show()
            }
        }

        val signUpButton = view.findViewById<Button>(R.id.sahipUyeOl)
        signUpButton.setOnClickListener {
            val nextFragment = halisahaUyeOlma()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment)
                .addToBackStack(null)
                .commit()
        }

        val forgotPasswordButton = view.findViewById<Button>(R.id.sahipSifremiUnuttum)
        forgotPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(requireContext(), "Lütfen email adresinizi giriniz", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(requireContext(), HaliSahaAnaSayfa::class.java)
                    startActivity(intent)
                    activity?.finish()
                } else {
                    Toast.makeText(requireContext(), "Giriş başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Şifre sıfırlama email'i gönderildi", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Email gönderilemedi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

