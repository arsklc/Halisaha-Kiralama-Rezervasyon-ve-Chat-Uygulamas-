package com.kilica.bitirmeproje.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kilica.bitirmeproje.R
import com.kilica.bitirmeproje.models.Ilan

class IlanAdapter(private var ilanList: List<Ilan>, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<IlanAdapter.IlanViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(ilan: Ilan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IlanViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_ilan, parent, false)
        return IlanViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: IlanViewHolder, position: Int) {
        holder.bind(ilanList[position])
    }

    override fun getItemCount() = ilanList.size

    fun updateIlanlar(ilanlar: List<Ilan>) {
        this.ilanList = ilanlar
        notifyDataSetChanged()
    }

    inner class IlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sahaTextView: TextView = itemView.findViewById(R.id.textViewSaha)
        private val saatTextView: TextView = itemView.findViewById(R.id.textViewSaat)
        private val mevkiTextView: TextView = itemView.findViewById(R.id.textViewMevki)
        private val notTextView: TextView = itemView.findViewById(R.id.textViewNot)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(ilanList[position])
                }
            }
        }

        fun bind(ilan: Ilan) {
            sahaTextView.text = ilan.saha
            saatTextView.text = ilan.saat
            mevkiTextView.text = ilan.mevki
            notTextView.text = ilan.not
        }
    }
}