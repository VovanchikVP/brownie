package com.worldclock.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app.R
import com.worldclock.app.data.MeterType
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

data class CostItem(
    val meterId: Long,
    val meterNumber: String,
    val meterAddress: String,
    val meterType: MeterType,
    val currentReading: Double,
    val previousReading: Double,
    val consumption: Double,
    val tariff: Double,
    val cost: Double,
    val readingDate: Long
)

class CostAdapter(
    private var costs: List<CostItem> = emptyList(),
    private val onItemClick: (CostItem) -> Unit = {}
) : RecyclerView.Adapter<CostAdapter.CostViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val decimalFormat = DecimalFormat("#,##0.00")

    class CostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewMeterType: ImageView = itemView.findViewById(R.id.imageViewMeterType)
        val textViewMeterNumber: TextView = itemView.findViewById(R.id.textViewMeterNumber)
        val textViewMeterAddress: TextView = itemView.findViewById(R.id.textViewMeterAddress)
        val textViewReadingDate: TextView = itemView.findViewById(R.id.textViewReadingDate)
        val textViewCost: TextView = itemView.findViewById(R.id.textViewCost)
        val textViewConsumption: TextView = itemView.findViewById(R.id.textViewConsumption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cost, parent, false)
        return CostViewHolder(view)
    }

    override fun onBindViewHolder(holder: CostViewHolder, position: Int) {
        val cost = costs[position]
        
        // Устанавливаем иконку в зависимости от типа прибора
        holder.imageViewMeterType.setImageResource(
            when (cost.meterType) {
                MeterType.ELECTRICITY -> R.drawable.ic_electricity
                MeterType.GAS -> R.drawable.ic_gas
                MeterType.HOT_WATER -> R.drawable.ic_hot_water
                MeterType.COLD_WATER -> R.drawable.ic_cold_water
            }
        )
        
        holder.textViewMeterNumber.text = cost.meterNumber
        holder.textViewMeterAddress.text = cost.meterAddress
        holder.textViewReadingDate.text = dateFormat.format(Date(cost.readingDate))
        holder.textViewCost.text = "${decimalFormat.format(cost.cost)} ₽"
        
        // Форматируем потребление в зависимости от типа прибора
        val consumptionText = when (cost.meterType) {
            MeterType.ELECTRICITY -> "${decimalFormat.format(cost.consumption)} кВт⋅ч"
            MeterType.GAS -> "${decimalFormat.format(cost.consumption)} м³"
            MeterType.HOT_WATER -> "${decimalFormat.format(cost.consumption)} м³"
            MeterType.COLD_WATER -> "${decimalFormat.format(cost.consumption)} м³"
        }
        holder.textViewConsumption.text = consumptionText
        
        // Добавляем обработчик клика
        holder.itemView.setOnClickListener {
            onItemClick(cost)
        }
    }

    override fun getItemCount(): Int = costs.size

    fun updateCosts(newCosts: List<CostItem>) {
        costs = newCosts
        notifyDataSetChanged()
    }
}