package com.kilica.bitirmeproje.fragments.kiraciscreenfrags

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kilica.bitirmeproje.R
import java.util.UUID

class ProfilEkrani : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var heightEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var editProfileIcon: ImageView
    private lateinit var addPhotoIcon: ImageView

    private lateinit var mediaLinearLayout: LinearLayout
    private var isEditing = false

    private val REQUEST_IMAGE_PICK = 3

    private val mediaUris = mutableListOf<Uri>()
    private val mediaUrls = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profil_ekrani, container, false)

        firstNameEditText = view.findViewById(R.id.editTextFirstName)
        lastNameEditText = view.findViewById(R.id.editTextLastName)
        ageEditText = view.findViewById(R.id.editTextAge)
        heightEditText = view.findViewById(R.id.editTextHeight)
        weightEditText = view.findViewById(R.id.editTextWeight)
        cityEditText = view.findViewById(R.id.editTextCity)
        editProfileIcon = view.findViewById(R.id.editProfileIcon)
        addPhotoIcon = view.findViewById(R.id.addMediaIcon)
        mediaLinearLayout = view.findViewById(R.id.mediaLinearLayout)

        loadUserProfile()

        editProfileIcon.setOnClickListener {
            if (isEditing) {
                updateUserProfile()
                endEditing()
            } else {
                enableEditing(true)
            }
        }

        addPhotoIcon.setOnClickListener {
            openGallery()
        }

        return view
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        firstNameEditText.setText(document.getString("firstName"))
                        lastNameEditText.setText(document.getString("lastName"))
                        ageEditText.setText(document.getString("age"))
                        heightEditText.setText(document.getString("height"))
                        weightEditText.setText(document.getString("weight"))
                        cityEditText.setText(document.getString("city"))

                        val mediaUrls = document.get("media") as? List<String>
                        if (mediaUrls != null) {
                            this.mediaUrls.clear()
                            this.mediaUris.clear()
                            mediaUrls.forEach { uriString ->
                                val uri = Uri.parse(uriString)
                                this.mediaUris.add(uri)
                                this.mediaUrls.add(uriString)
                                addMediaToScrollView(uri)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Profil bilgileri yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val user = hashMapOf(
                "firstName" to firstNameEditText.text.toString().trim(),
                "lastName" to lastNameEditText.text.toString().trim(),
                "age" to ageEditText.text.toString().trim(),
                "height" to heightEditText.text.toString().trim(),
                "weight" to weightEditText.text.toString().trim(),
                "city" to cityEditText.text.toString().trim(),
                "media" to mediaUrls
            )

            firestore.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener {
                    Toast.makeText(context, "Profil güncellendi!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Profil güncellenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun enableEditing(enable: Boolean) {
        firstNameEditText.isEnabled = enable
        lastNameEditText.isEnabled = enable
        ageEditText.isEnabled = enable
        heightEditText.isEnabled = enable
        weightEditText.isEnabled = enable
        cityEditText.isEnabled = enable
        isEditing = enable

        val icon = if (enable) R.drawable.ic_save else R.drawable.ic_edit
        editProfileIcon.setImageResource(icon)

        if (enable) {
            addPhotoIcon.visibility = View.VISIBLE
        } else {
            addPhotoIcon.visibility = View.GONE
        }
    }

    private fun endEditing() {
        enableEditing(false)
        editProfileIcon.setImageResource(R.drawable.ic_edit)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data!!
            mediaUris.add(imageUri)
            uploadImageToFirebase(imageUri)
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val storageRef = storage.reference
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val imageRef = storageRef.child("users/$userId/${UUID.randomUUID()}.jpg")
            val uploadTask = imageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    mediaUrls.add(uri.toString())
                    displaySelectedPhotos()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Görsel yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displaySelectedPhotos() {
        mediaLinearLayout.removeAllViews()
        for (uri in mediaUris) {
            addMediaToScrollView(uri)
        }
    }

    private fun addMediaToScrollView(mediaUri: Uri) {
        val mediaView = ImageView(context).apply {
            Glide.with(this@ProfilEkrani).load(mediaUri).into(this)
            setOnLongClickListener {
                showDeleteConfirmationDialog(mediaUri)
                true
            }
        }

        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels / 2
        val height = displayMetrics.heightPixels / 3

        mediaView.layoutParams = LinearLayout.LayoutParams(width, height).apply {
            marginEnd = 16
        }

        mediaLinearLayout.addView(mediaView)
    }

    private fun showDeleteConfirmationDialog(mediaUri: Uri) {
        AlertDialog.Builder(context).apply {
            setTitle("Görseli sil")
            setMessage("Bu görseli silmek istediğinizden emin misiniz?")
            setPositiveButton("Evet") { _, _ ->
                removeMediaFromList(mediaUri)
            }
            setNegativeButton("Hayır", null)
        }.show()
    }

    private fun removeMediaFromList(mediaUri: Uri) {
        val index = mediaUris.indexOf(mediaUri)
        if (index != -1) {
            mediaUris.removeAt(index)
            mediaUrls.removeAt(index)
            displaySelectedPhotos()
            updateUserProfile()
        } else {
            Toast.makeText(context, "Görsel bulunamadı", Toast.LENGTH_SHORT).show()
        }
    }
}
