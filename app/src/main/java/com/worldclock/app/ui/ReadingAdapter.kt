package com.worldclock.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app.R
import com.worldclock.app.data.Reading
import java.text.SimpleDateFormat
import java.util.*

class ReadingAdapter(
    private var readings: List<ReadingWithMeter>,
    private val onDeleteClick: (Reading) -> Unit
) : RecyclerView.Adapter<ReadingAdapter.ReadingViewHolder>() {

    data class ReadingWithMeter(
        val reading: Reading,
        val meterNumber: String,
        val meterType: String
    )

    class ReadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewReadingType: ImageView = itemView.findViewById(R.id.imageViewReadingType)
        val textViewReadingValue: TextView = itemView.findViewById(R.id.textViewReadingValue)
        val textViewMeterInfo: TextView = itemView.findViewById(R.id.textViewMeterInfo)
        val textViewReadingDate: TextView = itemView.findViewById(R.id.textViewReadingDate)
        val buttonDeleteReading: View = itemView.findViewById(R.id.buttonDeleteReading)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reading, parent, false)
        return ReadingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReadingViewHolder, position: Int) {
        val readingWithMeter = readings[position]
        val reading = readingWithMeter.reading
        
        // Устанавливаем иконку в зависимости от типа прибора
        val iconRes = when (readingWithMeter.meterType) {
            "Электричество" -> R.drawable.ic_electricity
            "Газ" -> R.drawable.ic_gas
            "Горячая вода" -> R.drawable.ic_hot_water
            "Холодная вода" -> R.drawable.ic_cold_water
            else -> R.drawable.ic_reading
        }
        holder.imageViewReadingType.setImageResource(iconRes)
        
        // Форматируем значение показания
        holder.textViewReadingValue.text = reading.value.toString()
        
        // Информация о приборе учета
        holder.textViewMeterInfo.text = "${readingWithMeter.meterNumber} (${readingWithMeter.meterType})"
        
        // Дата и время показания
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val dateTime = dateFormat.format(Date(reading.date))
        holder.textViewReadingDate.text = dateTime
        
        // Обработчик нажатия на кнопку удаления
        holder.buttonDeleteReading.setOnClickListener {
            onDeleteClick(reading)
        }
    }

    override fun getItemCount(): Int = readings.size

    fun updateReadings(newReadings: List<ReadingWithMeter>) {
        readings = newReadings
        notifyDataSetChanged()
    }
}