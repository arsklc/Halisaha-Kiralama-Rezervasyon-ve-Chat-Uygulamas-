package com.kilica.bitirmeproje.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kilica.bitirmeproje.adapters.DateAdapter
import com.kilica.bitirmeproje.adapters.SlotsAdapter
import com.kilica.bitirmeproje.databinding.ActivityHalisahaDetailBinding
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.FirebaseFirestore
import com.kilica.bitirmeproje.models.Slot

class HalisahaDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHalisahaDetailBinding
    private lateinit var slotsAdapter: SlotsAdapter
    private lateinit var dateAdapter: DateAdapter
    private val availableSlots = mutableListOf<Slot>()
    private val dates = mutableListOf<Date>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var currentUser: FirebaseUser
    private lateinit var photoContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHalisahaDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = FirebaseAuth.getInstance().currentUser ?: return

        val ownerId = intent.getStringExtra("ownerId")
        if (ownerId == null) {
            Toast.makeText(this, "Saha sahibi bilgisi alınamadı.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val name = intent.getStringExtra("halisaha_name")
        val city = intent.getStringExtra("halisaha_city")
        val district = intent.getStringExtra("halisaha_district")
        val imageUrl: String? = intent.getStringExtra("halisaha_imageUrl")
        val price = intent.getStringExtra("halisaha_price")
        val size = intent.getStringExtra("halisaha_size")
        val address = intent.getStringExtra("halisaha_address")
        val additionalInformation = intent.getStringExtra("halisaha_address")

        binding.textViewName.text = name
        binding.textViewCity.text = city
        binding.textViewDistrict.text = district
        binding.textViewPrice.text = price
        binding.textViewSize.text = size
        binding.textViewAddress.text = address
        binding.textViewEkbilgi.text = additionalInformation

        photoContainer = binding.photoContainer

        imageUrl?.let { loadPhotos(it) }

        binding.buttonChat.setOnClickListener {
            Log.d("ChatStart", "Starting chat with ownerId: $ownerId, chatId: ${name}-$city-$district")

            val intent = Intent(this, Chat::class.java).apply {
                putExtra("chatId", "$name-$city-$district")
                putExtra("receiverId", ownerId)
                putExtra("halisaha_name", name)
            }
            startActivity(intent)
        }

        binding.textViewAddress.setOnClickListener {
            val address = binding.textViewAddress.text.toString()
            val uri = Uri.parse("geo:0,0?q=$address")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Google Maps uygulaması bulunamadı", Toast.LENGTH_SHORT).show()
            }
        }

        slotsAdapter = SlotsAdapter(availableSlots) { slot ->
            requestSlotReservation(slot)
        }
        binding.recyclerViewSlots.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSlots.adapter = slotsAdapter

        dateAdapter = DateAdapter(dates) { date ->
            loadSlotsForDate(date)
        }
        binding.recyclerViewDates.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewDates.adapter = dateAdapter

        loadDates()
    }

    private fun loadPhotos(photoUrls: String?) {
        photoUrls?.let {
            photoContainer.removeAllViews()
            val imageView = ImageView(this)
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val screenWidth = displayMetrics.widthPixels
            val imageHeight = screenHeight / 3

            val params = LinearLayout.LayoutParams(
                screenWidth,
                imageHeight
            )
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(this).load(Uri.parse(it)).into(imageView)
            photoContainer.addView(imageView)
        }
    }

    private fun loadDates() {
        val today = Calendar.getInstance()
        for (i in 0..6) {
            val day = today.clone() as Calendar
            day.add(Calendar.DAY_OF_YEAR, i)
            dates.add(day.time)
        }
        dateAdapter.notifyDataSetChanged()
    }

    private fun loadSlotsForDate(date: Date) {
        availableSlots.clear()
        val calendar = Calendar.getInstance()
        calendar.time = date
        for (hour in 9..21) {
            checkIfSlotIsReserved(date, hour) { isReserved ->
                availableSlots.add(Slot(date, hour, isReserved))
                availableSlots.sortBy { it.hour }
                slotsAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun checkIfSlotIsReserved(date: Date, hour: Int, callback: (Boolean) -> Unit) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(date)
        val ownerId = intent.getStringExtra("ownerId")

        db.collection("reservations")
            .document("$dateString-$hour-$ownerId")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                callback(false)
            }
    }

    private fun requestSlotReservation(slot: Slot) {
        if (slot.isReserved) {
            Toast.makeText(this, "Bu tarih daha önce rezerve edilmiş.", Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(slot.date)

        val reservationRequest = hashMapOf(
            "date" to dateString,
            "hour" to slot.hour,
            "isReserved" to false,
            "requestedBy" to currentUser.uid,
            "halisahaName" to binding.textViewName.text.toString(),
            "halisahaId" to intent.getStringExtra("ownerId")
        )

        db.collection("reservation_requests")
            .add(reservationRequest)
            .addOnSuccessListener {
                Toast.makeText(this, "Rezervasyon talebi gönderildi.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Rezervasyon talebi gönderimi başarısız oldu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}