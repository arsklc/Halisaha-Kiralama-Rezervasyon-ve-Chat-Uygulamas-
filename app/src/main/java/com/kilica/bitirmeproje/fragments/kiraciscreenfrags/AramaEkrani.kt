package com.kilica.bitirmeproje.fragments.kiraciscreenfrags

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.activities.HalisahaDetailActivity
import com.kilica.bitirmeproje.adapters.HalisahaAdapter
import com.kilica.bitirmeproje.models.Halisaha
import java.util.Locale

class AramaEkrani : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var editTextCity: EditText
    private lateinit var editTextDistrict: EditText
    private lateinit var editTextSearch: EditText
    private lateinit var recyclerViewHalisahalar: RecyclerView
    private lateinit var adapter: HalisahaAdapter
    private var halisahalar: List<Halisaha> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_arama_ekrani, container, false)

        editTextCity = view.findViewById(R.id.editTextCity)
        editTextDistrict = view.findViewById(R.id.editTextDistrict)
        editTextSearch = view.findViewById(R.id.editTextSearch)
        recyclerViewHalisahalar = view.findViewById(R.id.recyclerViewHalisahalar)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewHalisahalar.layoutManager = LinearLayoutManager(context)
        adapter = HalisahaAdapter(halisahalar) { halisaha -> onHalisahaClicked(halisaha) }
        recyclerViewHalisahalar.adapter = adapter

        firestore = FirebaseFirestore.getInstance()

        loadHalisahalar()

        editTextCity.addTextChangedListener(textWatcher)
        editTextDistrict.addTextChangedListener(textWatcher)
        editTextSearch.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            filterHalisahalar()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun loadHalisahalar() {
        firestore.collection("halisaha").get()
            .addOnSuccessListener { result ->
                halisahalar = result.map { document ->
                    val photos = document.get("photos") as? List<String>
                    val imageUrl = if (photos.isNullOrEmpty()) "" else photos[0]

                    Halisaha(
                        name = document.getString("name") ?: "",
                        city = document.getString("city") ?: "",
                        district = document.getString("district") ?: "",
                        imageUrl = imageUrl,
                        price = document.getString("price") ?: "",
                        size = document.getString("size") ?: "",
                        address = document.getString("address") ?: "",
                        ownerId = document.getString("ownerId") ?: ""
                    )
                }
                adapter.updateHalisahalar(halisahalar)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Halı sahalar yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterHalisahalar() {
        val selectedCity = editTextCity.text.toString()
        val selectedDistrict = editTextDistrict.text.toString()
        val searchQuery = editTextSearch.text.toString().toLowerCase(Locale.getDefault())

        val filteredHalisahalar = halisahalar.filter { halisaha ->
            (selectedCity.isEmpty() || halisaha.city.contains(selectedCity, true)) &&
                    (selectedDistrict.isEmpty() || halisaha.district.contains(selectedDistrict, true)) &&
                    (searchQuery.isEmpty() || halisaha.name.toLowerCase(Locale.getDefault()).contains(searchQuery))
        }

        adapter.updateHalisahalar(filteredHalisahalar)
    }

    private fun onHalisahaClicked(halisaha: Halisaha) {
        val intent = Intent(context, HalisahaDetailActivity::class.java).apply {
            putExtra("halisaha_name", halisaha.name)
            putExtra("halisaha_city", halisaha.city)
            putExtra("halisaha_district", halisaha.district)
            putExtra("halisaha_size", halisaha.size)
            putExtra("halisaha_price", halisaha.price)
            putExtra("halisaha_address", halisaha.address)
            putExtra("halisaha_imageUrl", halisaha.imageUrl)
            putExtra("ownerId",halisaha.ownerId)
        }
        startActivity(intent)
    }
}
