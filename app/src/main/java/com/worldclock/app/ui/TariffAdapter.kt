package com.worldclock.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app.R
import com.worldclock.app.data.Tariff
import com.worldclock.app.data.MeterType
import java.text.SimpleDateFormat
import java.util.*

class TariffAdapter(
    private var tariffs: List<TariffWithMeter>,
    private val onDeleteClick: (Tariff) -> Unit
) : RecyclerView.Adapter<TariffAdapter.TariffViewHolder>() {

    data class TariffWithMeter(
        val tariff: Tariff,
        val meterNumber: String,
        val meterType: String
    )

    class TariffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewTariff: ImageView = itemView.findViewById(R.id.imageViewTariff)
        val textViewTariffRate: TextView = itemView.findViewById(R.id.textViewTariffRate)
        val textViewMeterInfo: TextView = itemView.findViewById(R.id.textViewMeterInfo)
        val textViewTariffPeriod: TextView = itemView.findViewById(R.id.textViewTariffPeriod)
        val buttonDeleteTariff: View = itemView.findViewById(R.id.buttonDeleteTariff)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TariffViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tariff, parent, false)
        return TariffViewHolder(view)
    }

    override fun onBindViewHolder(holder: TariffViewHolder, position: Int) {
        val tariffWithMeter = tariffs[position]
        val tariff = tariffWithMeter.tariff
        
        // Устанавливаем иконку в зависимости от типа прибора
        val iconRes = when (tariffWithMeter.meterType) {
            "Электричество" -> R.drawable.ic_electricity
            "Газ" -> R.drawable.ic_gas
            "Горячая вода" -> R.drawable.ic_hot_water
            "Холодная вода" -> R.drawable.ic_cold_water
            else -> R.drawable.ic_tariff
        }
        holder.imageViewTariff.setImageResource(iconRes)
        
        // Форматируем тариф
        holder.textViewTariffRate.text = "${tariff.rate} руб/ед.изм."
        
        // Информация о приборе учета
        holder.textViewMeterInfo.text = "${tariffWithMeter.meterNumber} (${tariffWithMeter.meterType})"
        
        // Период действия тарифа
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val startDate = dateFormat.format(Date(tariff.startDate))
        val endDateText = if (tariff.endDate != null) {
            " - ${dateFormat.format(Date(tariff.endDate))}"
        } else {
            " - Действует"
        }
        holder.textViewTariffPeriod.text = "$startDate$endDateText"
        
        // Обработчик нажатия на кнопку удаления
        holder.buttonDeleteTariff.setOnClickListener {
            onDeleteClick(tariff)
        }
    }

    override fun getItemCount(): Int = tariffs.size

    fun updateTariffs(newTariffs: List<TariffWithMeter>) {
        tariffs = newTariffs
        notifyDataSetChanged()
    }
}