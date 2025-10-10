package com.worldclock.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class AddressCostAdapter(
    private var addressCosts: List<AddressCostItem> = emptyList(),
    private val onItemClick: (AddressCostItem) -> Unit = {}
) : RecyclerView.Adapter<AddressCostAdapter.AddressCostViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val decimalFormat = DecimalFormat("#,##0.00")

    class AddressCostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewAddress: ImageView = itemView.findViewById(R.id.imageViewAddress)
        val textViewAddress: TextView = itemView.findViewById(R.id.textViewAddress)
        val textViewTotalCost: TextView = itemView.findViewById(R.id.textViewTotalCost)
        val textViewMetersCount: TextView = itemView.findViewById(R.id.textViewMetersCount)
        val textViewLatestDate: TextView = itemView.findViewById(R.id.textViewLatestDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressCostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_address_cost, parent, false)
        return AddressCostViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressCostViewHolder, position: Int) {
        val addressCost = addressCosts[position]
        
        // Устанавливаем иконку адреса
        holder.imageViewAddress.setImageResource(R.drawable.ic_location)
        
        holder.textViewAddress.text = addressCost.address
        holder.textViewTotalCost.text = "${decimalFormat.format(addressCost.totalCost)} ₽"
        holder.textViewMetersCount.text = "${addressCost.metersCount} приборов"
        
        // Находим самую позднюю дату показаний
        val latestDate = addressCost.meters.maxByOrNull { it.readingDate }?.readingDate
        if (latestDate != null) {
            holder.textViewLatestDate.text = "Обновлено: ${dateFormat.format(Date(latestDate))}"
        } else {
            holder.textViewLatestDate.text = ""
        }
        
        // Добавляем обработчик клика
        holder.itemView.setOnClickListener {
            onItemClick(addressCost)
        }
    }

    override fun getItemCount(): Int = addressCosts.size

    fun updateAddressCosts(newAddressCosts: List<AddressCostItem>) {
        addressCosts = newAddressCosts
        notifyDataSetChanged()
    }
}