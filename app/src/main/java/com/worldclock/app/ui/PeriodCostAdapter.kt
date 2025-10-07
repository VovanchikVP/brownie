package com.worldclock.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

data class PeriodCostItem(
    val currentReading: Double,
    val previousReading: Double,
    val consumption: Double,
    val tariff: Double,
    val cost: Double,
    val currentDate: Long,
    val previousDate: Long
)

class PeriodCostAdapter(
    private var periodCosts: List<PeriodCostItem> = emptyList()
) : RecyclerView.Adapter<PeriodCostAdapter.PeriodViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val decimalFormat = DecimalFormat("#,##0.00")

    class PeriodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewPeriod: TextView = itemView.findViewById(R.id.textViewPeriod)
        val textViewConsumption: TextView = itemView.findViewById(R.id.textViewConsumption)
        val textViewTariff: TextView = itemView.findViewById(R.id.textViewTariff)
        val textViewCost: TextView = itemView.findViewById(R.id.textViewCost)
        val textViewReadings: TextView = itemView.findViewById(R.id.textViewReadings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeriodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_period_cost, parent, false)
        return PeriodViewHolder(view)
    }

    override fun onBindViewHolder(holder: PeriodViewHolder, position: Int) {
        val periodCost = periodCosts[position]
        
        // Форматируем период
        val periodText = "${dateFormat.format(Date(periodCost.previousDate))} - ${dateFormat.format(Date(periodCost.currentDate))}"
        holder.textViewPeriod.text = periodText
        
        // Форматируем потребление
        holder.textViewConsumption.text = "${decimalFormat.format(periodCost.consumption)}"
        
        // Форматируем тариф
        holder.textViewTariff.text = "${decimalFormat.format(periodCost.tariff)} ₽"
        
        // Форматируем стоимость
        holder.textViewCost.text = "${decimalFormat.format(periodCost.cost)} ₽"
        
        // Форматируем показания
        val readingsText = "${decimalFormat.format(periodCost.previousReading)} → ${decimalFormat.format(periodCost.currentReading)}"
        holder.textViewReadings.text = readingsText
    }

    override fun getItemCount(): Int = periodCosts.size

    fun updatePeriodCosts(newPeriodCosts: List<PeriodCostItem>) {
        periodCosts = newPeriodCosts
        notifyDataSetChanged()
    }
}