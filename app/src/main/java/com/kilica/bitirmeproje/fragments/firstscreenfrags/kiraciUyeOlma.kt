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
import com.google.firebase.firestore.FirebaseFirestore
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.activities.KiraciAnaSayfa

class kiraciUyeOlma : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kiraci_uye_olma, container, false)

        val emailEditText = view.findViewById<EditText>(R.id.editTextText3)
        val passwordEditText = view.findViewById<EditText>(R.id.editTextTextPassword)
        val firstNameEditText = view.findViewById<EditText>(R.id.editTextText)
        val lastNameEditText = view.findViewById<EditText>(R.id.editTextText2)
        val registerButton = view.findViewById<Button>(R.id.kiraciKayit)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            val user = hashMapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "email" to email
                            )

                            if (userId != null) {
                                firestore.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                                        loginUser(email, password)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Kayıt başarısız: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(requireContext(), KiraciAnaSayfa::class.java)
                    startActivity(intent)
                    activity?.finish()
                } else {
                    Toast.makeText(requireContext(), "Giriş başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
