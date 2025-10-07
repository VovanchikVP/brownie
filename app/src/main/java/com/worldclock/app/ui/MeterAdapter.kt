package com.worldclock.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app.R
import com.worldclock.app.data.Meter
import com.worldclock.app.data.MeterType

class MeterAdapter(
    private var meters: List<Meter>,
    private val onDeleteClick: (Meter) -> Unit
) : RecyclerView.Adapter<MeterAdapter.MeterViewHolder>() {

    class MeterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewMeterType: ImageView = itemView.findViewById(R.id.imageViewMeterType)
        val textViewMeterNumber: TextView = itemView.findViewById(R.id.textViewMeterNumber)
        val textViewMeterType: TextView = itemView.findViewById(R.id.textViewMeterType)
        val textViewMeterAddress: TextView = itemView.findViewById(R.id.textViewMeterAddress)
        val buttonDeleteMeter: View = itemView.findViewById(R.id.buttonDeleteMeter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meter, parent, false)
        return MeterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeterViewHolder, position: Int) {
        val meter = meters[position]
        
        holder.textViewMeterNumber.text = meter.number
        holder.textViewMeterType.text = meter.type.displayName
        holder.textViewMeterAddress.text = meter.address
        
        // Устанавливаем иконку в зависимости от типа
        val iconRes = when (meter.type) {
            MeterType.ELECTRICITY -> R.drawable.ic_electricity
            MeterType.GAS -> R.drawable.ic_gas
            MeterType.HOT_WATER -> R.drawable.ic_hot_water
            MeterType.COLD_WATER -> R.drawable.ic_cold_water
        }
        holder.imageViewMeterType.setImageResource(iconRes)
        
        // Обработчик нажатия на кнопку удаления
        holder.buttonDeleteMeter.setOnClickListener {
            onDeleteClick(meter)
        }
    }

    override fun getItemCount(): Int = meters.size

    fun updateMeters(newMeters: List<Meter>) {
        meters = newMeters
        notifyDataSetChanged()
    }
}