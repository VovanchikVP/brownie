package com.worldclock.app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.worldclock.app.data.*
import com.worldclock.app.databinding.ActivityMeterCostsBinding
import com.worldclock.app.databinding.DialogAddReadingSimpleBinding
import com.worldclock.app.ui.PeriodCostAdapter
import com.worldclock.app.ui.PeriodCostItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MeterCostsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMeterCostsBinding
    private lateinit var database: AppDatabase
    private lateinit var repository: MeterRepository
    private lateinit var adapter: PeriodCostAdapter
    
    private var meterId: Long = 0
    private var meterNumber: String = ""
    private var meterAddress: String = ""
    private var meterType: MeterType = MeterType.ELECTRICITY
    
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeterCostsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Получаем данные о приборе учета
        getMeterData()
        
        // Инициализация базы данных
        initializeDatabase()
        
        // Настройка RecyclerView
        setupRecyclerView()
        
        // Настройка заголовка
        setupHeader()
        
        // Настройка обработчиков
        setupClickListeners()
        
        // Загружаем затраты по периодам
        loadPeriodCosts()
    }
    
    private fun getMeterData() {
        meterId = intent.getLongExtra("meter_id", 0)
        meterNumber = intent.getStringExtra("meter_number") ?: ""
        meterAddress = intent.getStringExtra("meter_address") ?: ""
        val meterTypeName = intent.getStringExtra("meter_type") ?: "ELECTRICITY"
        meterType = try {
            MeterType.valueOf(meterTypeName)
        } catch (e: Exception) {
            MeterType.ELECTRICITY
        }
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
        adapter = PeriodCostAdapter()
        binding.recyclerViewPeriods.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPeriods.adapter = adapter
    }
    
    private fun setupHeader() {
        binding.textViewMeterNumber.text = meterNumber
        binding.textViewMeterAddress.text = meterAddress
        binding.textViewMeterType.text = meterType.displayName
        
        // Устанавливаем иконку в зависимости от типа прибора
        binding.imageViewMeterType.setImageResource(
            when (meterType) {
                MeterType.ELECTRICITY -> R.drawable.ic_electricity
                MeterType.GAS -> R.drawable.ic_gas
                MeterType.HOT_WATER -> R.drawable.ic_hot_water
                MeterType.COLD_WATER -> R.drawable.ic_cold_water
            }
        )
    }
    
    private fun setupClickListeners() {
        binding.appNameText.setOnClickListener {
            // Возвращаемся на главную страницу
            finish()
        }
        
        binding.fabAddReading.setOnClickListener {
            showAddReadingDialog()
        }
    }
    
    private fun loadPeriodCosts() {
        lifecycleScope.launch {
            try {
                // Получаем все показания для прибора, отсортированные по дате
                val readings = repository.getReadingsByMeterId(meterId)
                readings.collect { readingsList ->
                    val sortedReadings = readingsList.sortedByDescending { it.date }
                    val periodCosts = mutableListOf<PeriodCostItem>()
                    
                    // Группируем показания по парам (текущее и предыдущее)
                    for (i in 0 until sortedReadings.size - 1) {
                        val currentReading = sortedReadings[i]
                        val previousReading = sortedReadings[i + 1]
                        
                        // Получаем тариф, действующий на дату текущего показания
                        val tariff = repository.getCurrentTariffByMeterId(meterId, currentReading.date)
                        
                        if (tariff != null) {
                            val consumption = currentReading.value - previousReading.value
                            val cost = consumption * tariff.rate
                            
                            val periodCost = PeriodCostItem(
                                currentReading = currentReading.value,
                                previousReading = previousReading.value,
                                consumption = consumption,
                                tariff = tariff.rate,
                                cost = cost,
                                currentDate = currentReading.date,
                                previousDate = previousReading.date
                            )
                            
                            periodCosts.add(periodCost)
                        }
                    }
                    
                    // Обновляем UI
                    adapter.updatePeriodCosts(periodCosts)
                    updateEmptyState(periodCosts.isEmpty())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewPeriods.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun showAddReadingDialog() {
        val dialogBinding = DialogAddReadingSimpleBinding.inflate(LayoutInflater.from(this))
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        // Устанавливаем информацию о приборе учета
        dialogBinding.textViewMeterInfo.text = "$meterNumber ($meterAddress)"
        
        // Устанавливаем текущую дату и время по умолчанию
        val currentDateTime = Calendar.getInstance()
        dialogBinding.editTextReadingDate.setText(dateTimeFormat.format(currentDateTime.time))
        
        // Настраиваем выбор даты и времени
        setupDateTimePicker(dialogBinding.editTextReadingDate)
        
        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.buttonSave.setOnClickListener {
            if (validateInput(dialogBinding)) {
                saveReading(dialogBinding)
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    private fun setupDateTimePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        
        editText.setOnClickListener {
            // Сначала выбираем дату
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    
                    // Затем выбираем время
                    TimePickerDialog(
                        this,
                        { _, hourOfDay, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)
                            editText.setText(dateTimeFormat.format(calendar.time))
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
    
    private fun validateInput(binding: DialogAddReadingSimpleBinding): Boolean {
        val valueText = binding.editTextReadingValue.text.toString().trim()
        val dateText = binding.editTextReadingDate.text.toString().trim()
        
        if (valueText.isEmpty()) {
            binding.editTextReadingValue.error = "Введите значение показания"
            return false
        }
        
        val value = valueText.toDoubleOrNull()
        if (value == null || value < 0) {
            binding.editTextReadingValue.error = "Введите корректное значение"
            return false
        }
        
        if (dateText.isEmpty()) {
            binding.editTextReadingDate.error = "Выберите дату и время"
            return false
        }
        
        return true
    }
    
    private fun saveReading(binding: DialogAddReadingSimpleBinding) {
        val value = binding.editTextReadingValue.text.toString().trim().toDouble()
        val dateText = binding.editTextReadingDate.text.toString().trim()
        
        // Парсим дату и время
        val date = try {
            dateTimeFormat.parse(dateText)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
        
        val reading = Reading(
            meterId = meterId,
            value = value,
            date = date
        )
        
        lifecycleScope.launch {
            try {
                repository.insertReading(reading)
                Toast.makeText(this@MeterCostsActivity, "Показание добавлено", Toast.LENGTH_SHORT).show()
                // Обновляем список затрат по периодам
                loadPeriodCosts()
            } catch (e: Exception) {
                Toast.makeText(this@MeterCostsActivity, "Ошибка при добавлении: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}