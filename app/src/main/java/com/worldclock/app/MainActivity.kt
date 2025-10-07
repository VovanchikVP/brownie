package com.worldclock.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldclock.app.data.*
import com.worldclock.app.databinding.ActivityMainBinding
import com.worldclock.app.databinding.DialogMenuBinding
import com.worldclock.app.ui.CostAdapter
import com.worldclock.app.ui.CostItem
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    // Database для управления приборами учета
    private lateinit var database: AppDatabase
    private lateinit var repository: MeterRepository
    private lateinit var costAdapter: CostAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Инициализация базы данных
        initializeDatabase()
        
        // Настройка RecyclerView для затрат
        setupCostsRecyclerView()
        
        // Настройка обработчиков
        setupClickListeners()
        
        // Загружаем затраты
        loadCosts()
    }
    
    private fun initializeDatabase() {
        database = AppDatabase.getDatabase(this)
        repository = MeterRepository(
            database.meterDao(),
            database.readingDao(),
            database.tariffDao()
        )
        
        // Добавляем тестовые данные
        addSampleData()
    }
    
    private fun setupCostsRecyclerView() {
        costAdapter = CostAdapter { costItem ->
            // Переходим к экрану затрат по периодам
            val intent = Intent(this, MeterCostsActivity::class.java).apply {
                putExtra("meter_id", costItem.meterId)
                putExtra("meter_number", costItem.meterNumber)
                putExtra("meter_address", costItem.meterAddress)
                putExtra("meter_type", costItem.meterType.name)
            }
            startActivity(intent)
        }
        binding.recyclerViewCosts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCosts.adapter = costAdapter
    }
    
    private fun setupClickListeners() {
        binding.menuButton.setOnClickListener {
            showMenu()
        }
    }
    
    private fun showMenu() {
        val dialogBinding = DialogMenuBinding.inflate(LayoutInflater.from(this))
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        dialogBinding.menuItemMeters.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MetersActivity::class.java)
            startActivity(intent)
        }
        
        dialogBinding.menuItemTariffs.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, TariffsActivity::class.java)
            startActivity(intent)
        }
        
        dialogBinding.menuItemReadings.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, ReadingsActivity::class.java)
            startActivity(intent)
        }
        
        dialog.show()
    }
    
    private fun addSampleData() {
        lifecycleScope.launch {
            try {
                // Проверяем, есть ли уже данные
                val existingMeters = repository.getAllMeters()
                existingMeters.collect { meters ->
                    if (meters.isEmpty()) {
                        // Добавляем приборы учета с разными адресами
                        val electricityMeterId = repository.insertMeter(
                            Meter(
                                number = "EL-001-2024",
                                address = "ул. Ленина, д. 10, кв. 5",
                                type = MeterType.ELECTRICITY
                            )
                        )
                        
                        val gasMeterId = repository.insertMeter(
                            Meter(
                                number = "GAS-002-2024",
                                address = "ул. Ленина, д. 10, кв. 5",
                                type = MeterType.GAS
                            )
                        )
                        
                        val hotWaterMeterId = repository.insertMeter(
                            Meter(
                                number = "HW-003-2024",
                                address = "ул. Пушкина, д. 25, кв. 12",
                                type = MeterType.HOT_WATER
                            )
                        )
                        
                        val coldWaterMeterId = repository.insertMeter(
                            Meter(
                                number = "CW-004-2024",
                                address = "ул. Пушкина, д. 25, кв. 12",
                                type = MeterType.COLD_WATER
                            )
                        )
                        
                        // Добавляем дополнительные приборы для демонстрации фильтра
                        val additionalElectricityMeterId = repository.insertMeter(
                            Meter(
                                number = "EL-005-2024",
                                address = "пр. Мира, д. 15, кв. 8",
                                type = MeterType.ELECTRICITY
                            )
                        )
                        
                        val additionalGasMeterId = repository.insertMeter(
                            Meter(
                                number = "GAS-006-2024",
                                address = "ул. Советская, д. 3, кв. 1",
                                type = MeterType.GAS
                            )
                        )
                        
                        // Добавляем показания (текущие и предыдущие для расчета)
                        val currentTime = System.currentTimeMillis()
                        val previousTime = currentTime - (30L * 24 * 60 * 60 * 1000) // 30 дней назад
                        val olderTime = previousTime - (30L * 24 * 60 * 60 * 1000) // 60 дней назад
                        
                        // Электричество - добавляем 3 показания для демонстрации периодов
                        repository.insertReading(Reading(meterId = electricityMeterId, value = 1150.0, date = olderTime))
                        repository.insertReading(Reading(meterId = electricityMeterId, value = 1200.0, date = previousTime))
                        repository.insertReading(Reading(meterId = electricityMeterId, value = 1250.5, date = currentTime))
                        
                        // Газ - добавляем 3 показания
                        repository.insertReading(Reading(meterId = gasMeterId, value = 750.0, date = olderTime))
                        repository.insertReading(Reading(meterId = gasMeterId, value = 800.0, date = previousTime))
                        repository.insertReading(Reading(meterId = gasMeterId, value = 850.2, date = currentTime))
                        
                        // Горячая вода - добавляем 3 показания
                        repository.insertReading(Reading(meterId = hotWaterMeterId, value = 80.0, date = olderTime))
                        repository.insertReading(Reading(meterId = hotWaterMeterId, value = 100.0, date = previousTime))
                        repository.insertReading(Reading(meterId = hotWaterMeterId, value = 120.8, date = currentTime))
                        
                        // Холодная вода - добавляем 3 показания
                        repository.insertReading(Reading(meterId = coldWaterMeterId, value = 60.0, date = olderTime))
                        repository.insertReading(Reading(meterId = coldWaterMeterId, value = 80.0, date = previousTime))
                        repository.insertReading(Reading(meterId = coldWaterMeterId, value = 95.3, date = currentTime))
                        
                        // Дополнительные приборы - добавляем показания
                        repository.insertReading(Reading(meterId = additionalElectricityMeterId, value = 2000.0, date = previousTime))
                        repository.insertReading(Reading(meterId = additionalElectricityMeterId, value = 2100.5, date = currentTime))
                        
                        repository.insertReading(Reading(meterId = additionalGasMeterId, value = 500.0, date = previousTime))
                        repository.insertReading(Reading(meterId = additionalGasMeterId, value = 550.2, date = currentTime))
                        
                        // Добавляем тарифы
                        val tariffStartDate = previousTime
                        repository.insertTariff(Tariff(meterId = electricityMeterId, rate = 4.5, startDate = tariffStartDate, endDate = null))
                        repository.insertTariff(Tariff(meterId = gasMeterId, rate = 6.2, startDate = tariffStartDate, endDate = null))
                        repository.insertTariff(Tariff(meterId = hotWaterMeterId, rate = 180.0, startDate = tariffStartDate, endDate = null))
                        repository.insertTariff(Tariff(meterId = coldWaterMeterId, rate = 45.0, startDate = tariffStartDate, endDate = null))
                        repository.insertTariff(Tariff(meterId = additionalElectricityMeterId, rate = 4.5, startDate = tariffStartDate, endDate = null))
                        repository.insertTariff(Tariff(meterId = additionalGasMeterId, rate = 6.2, startDate = tariffStartDate, endDate = null))
                    }
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadCosts() {
        lifecycleScope.launch {
            try {
                val meters = repository.getAllMeters()
                meters.collect { meterList ->
                    val costItems = mutableListOf<CostItem>()
                    
                    for (meter in meterList) {
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
                    costAdapter.updateCosts(costItems)
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
    
    override fun onResume() {
        super.onResume()
        // Обновляем затраты при возвращении на экран
        loadCosts()
    }
}