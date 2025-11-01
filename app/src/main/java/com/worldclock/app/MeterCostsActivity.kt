package com.worldclock.app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.AutoCompleteTextView
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.worldclock.app.data.*
import com.worldclock.app.databinding.ActivityMeterCostsBinding
import com.worldclock.app.databinding.DialogAddReadingBinding
import com.worldclock.app.ui.PeriodCostAdapter
import com.worldclock.app.ui.PeriodCostItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MeterCostsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMeterCostsBinding
    private lateinit var database: AppDatabase
    private lateinit var repository: MeterRepository
    private lateinit var periodCostAdapter: PeriodCostAdapter
    
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
        periodCostAdapter = PeriodCostAdapter()
        binding.recyclerViewPeriods.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPeriods.adapter = periodCostAdapter
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
        binding.imageViewBackButton.setOnClickListener {
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
                // Проверяем, что meterId валиден
                if (meterId == 0L) {
                    updateEmptyState(true)
                    return@launch
                }
                
                // Получаем все показания для прибора, отсортированные по дате
                val readings = repository.getReadingsByMeterId(meterId)
                readings.collect { readingsList ->
                    val sortedReadings = readingsList.sortedByDescending { it.date }
                    val periodCostItems = mutableListOf<PeriodCostItem>()
                    
                    // Для расчета периода нужно минимум 2 показания
                    if (sortedReadings.size < 2) {
                        withContext(Dispatchers.Main) {
                            updatePeriodCostsUI(periodCostItems)
                            updateEmptyState(true)
                        }
                        return@collect
                    }
                    
                    // Группируем показания по парам (текущее и предыдущее)
                    for (i in 0 until sortedReadings.size - 1) {
                        val currentReading = sortedReadings[i]
                        val previousReading = sortedReadings[i + 1]
                        
                        // Получаем тариф, действующий на дату текущего показания
                        val tariff = repository.getCurrentTariffByMeterId(meterId, currentReading.date)
                        Log.d("MeterCostsActivity", "Period $i: tariff=${tariff?.rate}, current=${currentReading.value}, previous=${previousReading.value}")
                        
                        if (tariff != null && currentReading.value >= previousReading.value) {
                            val consumption = currentReading.value - previousReading.value
                            val cost = consumption * tariff.rate
                            
                            val periodCostItem = PeriodCostItem(
                                currentReading = currentReading.value,
                                previousReading = previousReading.value,
                                consumption = consumption,
                                tariff = tariff.rate,
                                cost = cost,
                                currentDate = currentReading.date,
                                previousDate = previousReading.date
                            )
                            
                            periodCostItems.add(periodCostItem)
                            Log.d("MeterCostsActivity", "Added period cost item: cost=$cost, consumption=$consumption")
                        } else {
                            Log.d("MeterCostsActivity", "Skipped period $i: tariff=${tariff?.rate}, valid=${currentReading.value >= previousReading.value}")
                        }
                    }
                    
                    // Обновляем UI на главном потоке
                    withContext(Dispatchers.Main) {
                        updatePeriodCostsUI(periodCostItems)
                        updateEmptyState(periodCostItems.isEmpty())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MeterCostsActivity", "Error loading period costs", e)
                withContext(Dispatchers.Main) {
                    updateEmptyState(true)
                }
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewPeriods.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun updatePeriodCostsUI(periodCosts: List<PeriodCostItem>) {
        // Обновляем адаптер с новыми данными
        Log.d("MeterCostsActivity", "Updating UI with ${periodCosts.size} period costs")
        periodCostAdapter.updatePeriodCosts(periodCosts)
        // Принудительно уведомляем RecyclerView об обновлении
        binding.recyclerViewPeriods.invalidate()
    }
    
    private fun showAddReadingDialog() {
        val dialogBinding = DialogAddReadingBinding.inflate(LayoutInflater.from(this))
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        // Скрываем фильтр по адресу, так как прибор уже выбран
        dialogBinding.editTextAddressFilter.let { addressFilter ->
            // Скрываем TextInputLayout (родительский элемент editTextAddressFilter)
            val textInputLayout = addressFilter.parent.parent as? com.google.android.material.textfield.TextInputLayout
            textInputLayout?.visibility = View.GONE
            
            // Скрываем TextView заголовка фильтра (он находится перед TextInputLayout)
            val linearLayout = dialogBinding.root as? android.widget.LinearLayout
            linearLayout?.let { layout ->
                for (i in 0 until layout.childCount) {
                    val child = layout.getChildAt(i)
                    if (child == textInputLayout && i > 0) {
                        // Скрываем предыдущий TextView (заголовок)
                        val previousChild = layout.getChildAt(i - 1)
                        if (previousChild is TextView) {
                            previousChild.visibility = View.GONE
                        }
                        break
                    }
                }
            }
        }
        
        // Предустанавливаем выбранный прибор и делаем его неактивным
        val meterDisplayText = "${meterNumber} (${meterType.displayName}) - ${meterAddress}"
        dialogBinding.autoCompleteMeter.setText(meterDisplayText, false)
        dialogBinding.autoCompleteMeter.isEnabled = false
        dialogBinding.autoCompleteMeter.isFocusable = false
        dialogBinding.autoCompleteMeter.isClickable = false
        
        // Скрываем родительский контейнер для выбора прибора (опционально, для упрощения UI)
        // Можно оставить видимым, чтобы показать, для какого прибора добавляется показание
        
        // Устанавливаем текущую дату и время по умолчанию
        val currentDateTime = Calendar.getInstance()
        dialogBinding.editTextReadingDate.setText(dateTimeFormat.format(currentDateTime.time))
        
        setupDateTimePicker(dialogBinding.editTextReadingDate)
        
        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.buttonSave.setOnClickListener {
            if (validateReadingInput(dialogBinding)) {
                saveReading(dialogBinding)
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    private fun validateReadingInput(binding: DialogAddReadingBinding): Boolean {
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
    
    private fun saveReading(binding: DialogAddReadingBinding) {
        val value = binding.editTextReadingValue.text.toString().trim().toDoubleOrNull()
            ?: run {
                Toast.makeText(this, "Ошибка: некорректное значение показания", Toast.LENGTH_LONG).show()
                return
            }
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
                // Перезагружаем затраты по периодам
                loadPeriodCosts()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MeterCostsActivity, "Ошибка при добавлении: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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
}