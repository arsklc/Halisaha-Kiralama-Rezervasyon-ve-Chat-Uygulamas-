package com.kilica.bitirmeproje.fragments.kiraciscreenfrags

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.activities.IlanDetay
import com.kilica.bitirmeproje.adapters.IlanAdapter
import com.kilica.bitirmeproje.models.Ilan

class IlanEkrani : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var ilanAdapter: IlanAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var addButton: ImageButton
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ilan_ekrani, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewIlanlar)
        recyclerView.layoutManager = LinearLayoutManager(context)
        addButton = view.findViewById(R.id.addButton)

        currentUser = FirebaseAuth.getInstance().currentUser!!
        ilanAdapter = IlanAdapter(emptyList(), object : IlanAdapter.OnItemClickListener {
            override fun onItemClick(ilan: Ilan) {
                val intent = Intent(context, IlanDetay::class.java)
                intent.putExtra("ilanId", ilan.ilanId)
                startActivity(intent)
            }
        })
        recyclerView.adapter = ilanAdapter
        firestore = FirebaseFirestore.getInstance()
        loadIlanlar()

        checkIfUserHasPublishedIlan()

        return view
    }

    private fun loadIlanlar() {
        firestore.collection("ilanlar")
            .get()
            .addOnSuccessListener { result ->
                val ilanList = result.map { document ->
                    Ilan(
                        ilanId = document.id,
                        saha = document.getString("saha") ?: "",
                        saat = document.getString("saat") ?: "",
                        mevki = document.getString("mevki") ?: "",
                        not = document.getString("not") ?: ""
                    )
                }
                ilanAdapter.updateIlanlar(ilanList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "İlanlar yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfUserHasPublishedIlan() {
        firestore.collection("ilanlar")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    addButton.setImageResource(R.drawable.yayinlanmis)
                    addButton.setOnClickListener {
                        showUserIlanPopup(result.documents[0].id)
                    }
                } else {
                    addButton.setImageResource(R.drawable.ekle)
                    addButton.setOnClickListener {
                        showAddIlanPopup()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddIlanPopup() {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.popup_add_ilan, null)
        builder.setView(view)

        val sahaEditText = view.findViewById<EditText>(R.id.editTextSaha)
        val saatEditText = view.findViewById<EditText>(R.id.editTextSaat)
        val mevkiEditText = view.findViewById<EditText>(R.id.editTextMevki)
        val notEditText = view.findViewById<EditText>(R.id.editTextNot)
        val publishButton = view.findViewById<Button>(R.id.buttonPublish)

        val dialog = builder.create()

        publishButton.setOnClickListener {
            val saha = sahaEditText.text.toString()
            val saat = saatEditText.text.toString()
            val mevki = mevkiEditText.text.toString()
            val not = notEditText.text.toString()

            val ilan = hashMapOf(
                "saha" to saha,
                "saat" to saat,
                "mevki" to mevki,
                "not" to not,
                "userId" to currentUser.uid
            )

            firestore.collection("ilanlar")
                .add(ilan)
                .addOnSuccessListener {
                    Toast.makeText(context, "İlan başarıyla yayınlandı.", Toast.LENGTH_SHORT).show()
                    loadIlanlar()
                    dialog.dismiss()
                    checkIfUserHasPublishedIlan()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "İlan yayınlanamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun showUserIlanPopup(ilanId: String) {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.popup_user_ilan, null)
        builder.setView(view)

        val sahaEditText = view.findViewById<EditText>(R.id.editTextSaha)
        val saatEditText = view.findViewById<EditText>(R.id.editTextSaat)
        val mevkiEditText = view.findViewById<EditText>(R.id.editTextMevki)
        val notEditText = view.findViewById<EditText>(R.id.editTextNot)
        val updateButton = view.findViewById<Button>(R.id.buttonUpdate)
        val deleteButton = view.findViewById<Button>(R.id.buttonDelete)

        firestore.collection("ilanlar").document(ilanId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    sahaEditText.setText(document.getString("saha"))
                    saatEditText.setText(document.getString("saat"))
                    mevkiEditText.setText(document.getString("mevki"))
                    notEditText.setText(document.getString("not"))
                } else {
                    Toast.makeText(context, "İlan bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "İlan detayları yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        val dialog = builder.create()

        updateButton.setOnClickListener {
            val updatedSaha = sahaEditText.text.toString()
            val updatedSaat = saatEditText.text.toString()
            val updatedMevki = mevkiEditText.text.toString()
            val updatedNot = notEditText.text.toString()

            val updatedIlan = mapOf(
                "saha" to updatedSaha,
                "saat" to updatedSaat,
                "mevki" to updatedMevki,
                "not" to updatedNot
            )

            firestore.collection("ilanlar").document(ilanId)
                .update(updatedIlan)
                .addOnSuccessListener {
                    Toast.makeText(context, "İlan başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
                    loadIlanlar()
                    checkIfUserHasPublishedIlan()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "İlan güncellenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        deleteButton.setOnClickListener {
            firestore.collection("ilanlar").document(ilanId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "İlan başarıyla silindi.", Toast.LENGTH_SHORT).show()
                    loadIlanlar()
                    checkIfUserHasPublishedIlan()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "İlan silinemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }
}