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
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val decimalFormat = DecimalFormat("#,##0.00")
    
    // Константы для типов элементов
    companion object {
        private const val TYPE_NOTIFICATION = 0
        private const val TYPE_ADDRESS_COST = 1
    }
    
    // Переменная для хранения уведомления
    private var notificationView: View? = null

    class AddressCostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewAddress: ImageView = itemView.findViewById(R.id.imageViewAddress)
        val textViewAddress: TextView = itemView.findViewById(R.id.textViewAddress)
        val textViewTotalCost: TextView = itemView.findViewById(R.id.textViewTotalCost)
        val textViewMetersCount: TextView = itemView.findViewById(R.id.textViewMetersCount)
        val textViewLatestDate: TextView = itemView.findViewById(R.id.textViewLatestDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_NOTIFICATION -> {
                val view = notificationView ?: throw IllegalStateException("Notification view not set")
                // Устанавливаем match_parent для уведомления
                val layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                view.layoutParams = layoutParams
                object : RecyclerView.ViewHolder(view) {}
            }
            TYPE_ADDRESS_COST -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_address_cost, parent, false)
                AddressCostViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AddressCostViewHolder -> {
                val addressCost = addressCosts[position - if (notificationView != null) 1 else 0]
                
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
        }
    }

    override fun getItemCount(): Int = addressCosts.size + if (notificationView != null) 1 else 0
    
    override fun getItemViewType(position: Int): Int {
        return if (notificationView != null && position == 0) {
            TYPE_NOTIFICATION
        } else {
            TYPE_ADDRESS_COST
        }
    }

    fun updateAddressCosts(newAddressCosts: List<AddressCostItem>) {
        addressCosts = newAddressCosts
        notifyDataSetChanged()
    }
    
    fun addNotificationItem(view: View) {
        notificationView = view
        notifyItemInserted(0)
    }
    
    fun removeNotificationItem() {
        if (notificationView != null) {
            notificationView = null
            notifyItemRemoved(0)
        }
    }
}