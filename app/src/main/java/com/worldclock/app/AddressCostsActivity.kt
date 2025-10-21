package com.worldclock.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldclock.app.data.*
import com.worldclock.app.databinding.ActivityAddressCostsBinding
import com.worldclock.app.ui.CostAdapter
import com.worldclock.app.ui.CostItem
import kotlinx.coroutines.launch

class AddressCostsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAddressCostsBinding
    private lateinit var database: AppDatabase
    private lateinit var repository: MeterRepository
    private lateinit var adapter: CostAdapter
    
    private var address: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressCostsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Получаем адрес из Intent
        address = intent.getStringExtra("address") ?: ""
        
        // Инициализация базы данных
        initializeDatabase()
        
        // Настройка RecyclerView
        setupRecyclerView()
        
        // Настройка заголовка
        setupHeader()
        
        // Настройка обработчиков
        setupClickListeners()
        
        // Загружаем затраты по адресу
        loadAddressCosts()
    }
    
    private fun initializeDatabase() {
        database = AppDatabase.getDatabase(this)
        repository = MeterRepository(
            database.meterDao(),
            database.readingDao(),
            database.tariffDao()
        )
    }
    
    private fun setupRecyclerView() {
        adapter = CostAdapter { costItem ->
            // Переходим к экрану затрат по периодам
            val intent = android.content.Intent(this, MeterCostsActivity::class.java).apply {
                putExtra("meter_id", costItem.meterId)
                putExtra("meter_number", costItem.meterNumber)
                putExtra("meter_address", costItem.meterAddress)
                putExtra("meter_type", costItem.meterType.name)
            }
            startActivity(intent)
        }
        binding.recyclerViewCosts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCosts.adapter = adapter
    }
    
    private fun setupHeader() {
        binding.textViewAddress.text = address
    }
    
    private fun setupClickListeners() {
        binding.imageViewBackButton.setOnClickListener {
            // Возвращаемся на главную страницу
            finish()
        }
    }
    
    private fun loadAddressCosts() {
        lifecycleScope.launch {
            try {
                val meters = repository.getAllMeters()
                meters.collect { meterList ->
                    val addressMeters = meterList.filter { it.address == address }
                    val costItems = mutableListOf<com.worldclock.app.ui.CostItem>()
                    
                    for (meter in addressMeters) {
                        // Получаем текущие и предыдущие показания
                        val currentReading = repository.getLatestReadingByMeterId(meter.id)
                        val previousReading = repository.getPreviousReadingByMeterId(meter.id)
                        
                        // Получаем текущий тариф
                        val currentTariff = repository.getCurrentTariffByMeterId(meter.id)
                        
                        // Если есть текущие показания, предыдущие показания и тариф
                        if (currentReading != null && previousReading != null && currentTariff != null) {
                            val consumption = currentReading.value - previousReading.value
                            val cost = consumption * currentTariff.rate
                            
                            val costItem = CostItem(
                                meterId = meter.id,
                                meterNumber = meter.number,
                                meterAddress = meter.address,
                                meterType = meter.type,
                                currentReading = currentReading.value,
                                previousReading = previousReading.value,
                                consumption = consumption,
                                tariff = currentTariff.rate,
                                cost = cost,
                                readingDate = currentReading.date
                            )
                            
                            costItems.add(costItem)
                        }
                    }
                    
                    // Обновляем UI
                    adapter.updateCosts(costItems)
                    updateEmptyState(costItems.isEmpty())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewCosts.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}