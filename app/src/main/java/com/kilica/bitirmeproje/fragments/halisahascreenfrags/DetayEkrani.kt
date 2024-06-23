package com.kilica.bitirmeproje.fragments.halisahascreenfrags

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kilica.bitirmeproje.R
import java.util.UUID

class DetayEkrani : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private lateinit var nameEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var sizeEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var cityAutoComplete: AutoCompleteTextView
    private lateinit var districtAutoComplete: AutoCompleteTextView
    private lateinit var editHalisahaIcon: ImageView
    private lateinit var photoContainer: LinearLayout
    private lateinit var addPhotoButton: ImageButton
    private lateinit var additionalFieldEditText: EditText

    private var isEditing = false
    private val PICK_IMAGE_REQUEST = 1
    private val selectedPhotos = mutableListOf<Uri>()
    private val photoUrls = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detay_ekrani, container, false)

        nameEditText = view.findViewById(R.id.editTextName)
        addressEditText = view.findViewById(R.id.editTextAddress)
        sizeEditText = view.findViewById(R.id.editTextSize)
        priceEditText = view.findViewById(R.id.editTextPrice)
        cityAutoComplete = view.findViewById(R.id.autoCompleteCity)
        districtAutoComplete = view.findViewById(R.id.autoCompleteDistrict)
        editHalisahaIcon = view.findViewById(R.id.editHalisahaIcon)
        photoContainer = view.findViewById(R.id.photoContainer)
        addPhotoButton = view.findViewById(R.id.addPhotoButton)
        additionalFieldEditText = view.findViewById(R.id.editTextAdditionalField)

        loadHalisahaDetails()
        loadCities()

        editHalisahaIcon.setOnClickListener {
            if (isEditing) {
                uploadPhotosAndSaveDetails()
            } else {
                enableEditing(true)
            }
        }

        addPhotoButton.setOnClickListener {
            openGallery()
        }

        cityAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedCity = cityAutoComplete.adapter.getItem(position) as String
            loadDistricts(selectedCity)
        }

        return view
    }

    private fun loadHalisahaDetails() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("halisaha").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        nameEditText.setText(document.getString("name"))
                        addressEditText.setText(document.getString("address"))
                        sizeEditText.setText(document.getString("size"))
                        priceEditText.setText(document.getString("price"))
                        cityAutoComplete.setText(document.getString("city"), false)
                        districtAutoComplete.setText(document.getString("district"), false)
                        val existingPhotoUrls = document.get("photos") as? List<String>
                        if (existingPhotoUrls != null) {
                            photoUrls.clear()
                            photoUrls.addAll(existingPhotoUrls)
                            loadPhotos(existingPhotoUrls)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Halı saha bilgileri yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateHalisahaDetails() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val halisahaDetails = hashMapOf(
                "name" to nameEditText.text.toString().trim(),
                "address" to addressEditText.text.toString().trim(),
                "size" to sizeEditText.text.toString().trim(),
                "price" to priceEditText.text.toString().trim(),
                "city" to cityAutoComplete.text.toString().trim(),
                "district" to districtAutoComplete.text.toString().trim(),
                "additionalField" to additionalFieldEditText.text.toString().trim(),
                "photos" to photoUrls,
                "ownerId" to userId
            )
            firestore.collection("halisaha").document(userId)
                .set(halisahaDetails)
                .addOnSuccessListener {
                    Toast.makeText(context, "Halı saha bilgileri güncellendi!", Toast.LENGTH_SHORT).show()
                    endEditing()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Halı saha bilgileri güncellenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadPhotos(photoUrls: List<String>) {
        photoContainer.removeAllViews()
        for (url in photoUrls) {
            val imageView = ImageView(context)
            val params = LinearLayout.LayoutParams(
                getScreenWidth(),
                getScreenHeight() / 3
            )
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(this).load(url).placeholder(R.drawable.fotoekle).into(imageView)
            imageView.setOnLongClickListener {
                showDeleteConfirmationDialog(url)
                true
            }
            photoContainer.addView(imageView)
        }
    }

    private fun enableEditing(enable: Boolean) {
        nameEditText.isEnabled = enable
        addressEditText.isEnabled = enable
        sizeEditText.isEnabled = enable
        priceEditText.isEnabled = enable
        cityAutoComplete.isEnabled = enable
        districtAutoComplete.isEnabled = enable
        addPhotoButton.visibility = if (enable) View.VISIBLE else View.GONE
        isEditing = enable

        val icon = if (enable) R.drawable.ic_save else R.drawable.ic_edit
        editHalisahaIcon.setImageResource(icon)
    }

    private fun endEditing() {
        enableEditing(false)
        editHalisahaIcon.setImageResource(R.drawable.ic_edit)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data!!
            selectedPhotos.add(imageUri)
            displaySelectedPhotos()
        }
    }

    private fun displaySelectedPhotos() {

        for (uri in selectedPhotos) {
            val imageView = ImageView(context)
            val params = LinearLayout.LayoutParams(
                getScreenWidth(),
                getScreenHeight() / 3
            )
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(this).load(uri).into(imageView)
            imageView.setOnLongClickListener {
                showDeleteConfirmationDialog(uri.toString())
                true
            }
            photoContainer.addView(imageView)
        }
    }

    private fun uploadPhotosAndSaveDetails() {
        if (selectedPhotos.isEmpty()) {
            updateHalisahaDetails()
            return
        }

        val initialPhotoUrls = ArrayList(photoUrls)
        photoUrls.clear()

        for (photoUri in selectedPhotos) {
            val fileName = UUID.randomUUID().toString()
            val photoRef = storageReference.child("photos/$fileName")

            photoRef.putFile(photoUri)
                .addOnSuccessListener { taskSnapshot ->
                    photoRef.downloadUrl.addOnSuccessListener { uri ->
                        photoUrls.add(uri.toString())
                        if (photoUrls.size == selectedPhotos.size) {
                            photoUrls.addAll(0, initialPhotoUrls)
                            updateHalisahaDetails()
                            selectedPhotos.clear()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Fotoğraf yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadCities() {
        firestore.collection("cities").get()
            .addOnSuccessListener { result ->
                val cities = result.map { it.getString("name") ?: "" }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)
                cityAutoComplete.setAdapter(adapter)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Şehirler yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadDistricts(city: String) {
        firestore.collection("districts").whereEqualTo("city", city).get()
            .addOnSuccessListener { result ->
                val districts = result.map { it.getString("name") ?: "" }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, districts)
                districtAutoComplete.setAdapter(adapter)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "İlçeler yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    private fun getScreenHeight(): Int {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    private fun showDeleteConfirmationDialog(url: String) {
        AlertDialog.Builder(requireContext())
            .setMessage("Fotoğrafı kaldırmak istediğinize emin misiniz?")
            .setPositiveButton("Evet") { _, _ ->
                removePhotoFromView(url)
            }
            .setNegativeButton("Hayır", null)
            .show()
    }

    private fun removePhotoFromView(url: String) {
        val index = photoUrls.indexOf(url)
        if (index != -1) {
            photoUrls.removeAt(index)
            loadPhotos(photoUrls)
        }
    }
}
