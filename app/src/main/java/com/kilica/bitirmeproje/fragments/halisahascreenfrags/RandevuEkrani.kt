package com.kilica.bitirmeproje.fragments.halisahascreenfrags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kilica.bitirmeproje.R
import java.text.SimpleDateFormat
import java.util.*
import com.kilica.bitirmeproje.adapters.DateAdapter
import com.kilica.bitirmeproje.adapters.ReservationRequestsAdapter
import com.kilica.bitirmeproje.adapters.SlotsAdapter
import com.kilica.bitirmeproje.models.ReservationRequest
import com.kilica.bitirmeproje.models.Slot

class RandevuEkrani : Fragment() {

    private lateinit var slotsAdapter: SlotsAdapter
    private lateinit var dateAdapter: DateAdapter
    private lateinit var requestsAdapter: ReservationRequestsAdapter
    private val availableSlots = mutableListOf<Slot>()
    private val dates = mutableListOf<Date>()
    private val reservationRequests = mutableListOf<ReservationRequest>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_randevu_ekrani, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerViewDates: RecyclerView = view.findViewById(R.id.recycler_view_dates)
        val recyclerViewSlots: RecyclerView = view.findViewById(R.id.recycler_view_slots)
        val recyclerViewRequests: RecyclerView = view.findViewById(R.id.recycler_view_requests)

        slotsAdapter = SlotsAdapter(availableSlots) { slot ->
            processReservationRequest(slot)
        }
        recyclerViewSlots.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewSlots.adapter = slotsAdapter

        dateAdapter = DateAdapter(dates) { date ->
            loadSlotsForDate(date)
        }
        recyclerViewDates.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewDates.adapter = dateAdapter

        requestsAdapter = ReservationRequestsAdapter(reservationRequests) { request, isAccepted ->
            handleReservationRequest(request, isAccepted)
        }
        recyclerViewRequests.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewRequests.adapter = requestsAdapter

        loadDates()
        loadReservationRequests()
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
        val ownerId = FirebaseAuth.getInstance().currentUser?.uid

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


    private fun processReservationRequest(slot: Slot) {
        slot.isReserved = !slot.isReserved
        slotsAdapter.notifyDataSetChanged()

        updateReservationStatus(slot)
    }

    private fun updateReservationStatus(slot: Slot) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = dateFormat.format(slot.date)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        if (slot.isReserved) {
            val reservation = hashMapOf(
                "date" to dateString,
                "hour" to slot.hour,
                "isReserved" to true,
                "halisaha_id" to userId
            )

            db.collection("reservations")
                .document("$dateString-${slot.hour}")
                .set(reservation)
                .addOnSuccessListener {

                }
                .addOnFailureListener { e ->

                }
        } else {
            db.collection("reservations")
                .document("$dateString-${slot.hour}")
                .delete()
                .addOnSuccessListener {

                }
                .addOnFailureListener { e ->

                }
        }
    }


    private fun loadReservationRequests() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("reservation_requests")
            .whereEqualTo("halisahaId", currentUserId)
            .get()
            .addOnSuccessListener { result ->
                reservationRequests.clear()
                for (document in result) {
                    val request = document.toObject(ReservationRequest::class.java)
                    request.id = document.id
                    reservationRequests.add(request)
                }
                requestsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->

            }
    }

    private fun handleReservationRequest(request: ReservationRequest, isAccepted: Boolean) {
        if (isAccepted) {
            val reservation = hashMapOf(
                "date" to request.date,
                "hour" to request.hour,
                "isReserved" to true,
                "reservedBy" to request.requestedBy,
                "halisahaName" to request.halisahaName,
                "halisahaId" to FirebaseAuth.getInstance().currentUser?.uid
            )

            db.collection("reservations")
                .document("${request.date}-${request.hour}-${FirebaseAuth.getInstance().currentUser?.uid}")
                .set(reservation)
                .addOnSuccessListener {
                    db.collection("reservation_requests")
                        .document(request.id)
                        .delete()
                        .addOnSuccessListener {
                            reservationRequests.remove(request)
                            requestsAdapter.notifyDataSetChanged()
                            Toast.makeText(
                                requireContext(),
                                "Rezervasyon talebi kabul edildi",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                }
        } else {
            db.collection("reservation_requests")
                .document(request.id)
                .delete()
                .addOnSuccessListener {
                    reservationRequests.remove(request)
                    requestsAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Rezervasyon talebi reddedildi.", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener { e ->
                }
        }
    }
}