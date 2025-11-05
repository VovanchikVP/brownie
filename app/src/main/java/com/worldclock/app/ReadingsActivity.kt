package com.worldclock.app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.worldclock.app.data.*
import com.worldclock.app.databinding.ActivityReadingsBinding
import com.worldclock.app.databinding.DialogAddReadingBinding
import com.worldclock.app.ui.ReadingAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ReadingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityReadingsBinding
    private lateinit var database: AppDatabase
    private lateinit var repository: MeterRepository
    private lateinit var adapter: ReadingAdapter
    private var meters: List<Meter> = emptyList()
    private var filteredMeters: List<Meter> = emptyList()
    private var meterAdapter: ArrayAdapter<String>? = null
    
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeDatabase()
        setupRecyclerView()
        setupClickListeners()
        loadReadings()
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
        adapter = ReadingAdapter(
            readings = emptyList(),
            onDeleteClick = { reading -> showDeleteConfirmation(reading) }
        )
        
        binding.recyclerViewReadings.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReadings.adapter = adapter
    }
    
    private fun setupClickListeners() {
        binding.fabAddReading.setOnClickListener {
            showAddReadingDialog()
        }
        
        binding.imageViewBackButton.setOnClickListener {
            // Возвращаемся на главную страницу
            finish()
        }
    }
    
    private fun loadReadings() {
        lifecycleScope.launch {
            try {
                // Загружаем приборы учета
                repository.getAllMeters().collect { metersList ->
                    meters = metersList
                    
                    // Загружаем показания
                    repository.getAllReadings().collect { readings ->
                        val readingsWithMeters = readings.map { reading ->
                            val meter = meters.find { it.id == reading.meterId }
                            ReadingAdapter.ReadingWithMeter(
                                reading = reading,
                                meterNumber = meter?.number ?: "Неизвестно",
                                meterType = meter?.type?.displayName ?: "Неизвестно"
                            )
                        }
                        
                        // Сортируем по дате (новые сверху)
                        val sortedReadings = readingsWithMeters.sortedByDescending { it.reading.date }
                        
                        adapter.updateReadings(sortedReadings)
                        updateEmptyState(sortedReadings.isEmpty())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewReadings.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun showAddReadingDialog() {
        val dialogBinding = DialogAddReadingBinding.inflate(LayoutInflater.from(this))
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        setupMeterDropdown(dialogBinding.autoCompleteMeter)
        setupAddressFilter(dialogBinding.editTextAddressFilter, dialogBinding.autoCompleteMeter)
        
        // Добавляем слушатель на выбор прибора для динамического обновления последних показаний
        setupMeterSelectionListener(dialogBinding)
        
        // Устанавливаем текущую дату и время по умолчанию
        val currentDateTime = Calendar.getInstance()
        dialogBinding.editTextReadingDate.setText(dateTimeFormat.format(currentDateTime.time))
        
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
    
    private fun setupMeterDropdown(autoComplete: AutoCompleteTextView) {
        filteredMeters = meters
        updateMeterDropdown(autoComplete)
    }
    
    private fun updateMeterDropdown(autoComplete: AutoCompleteTextView) {
        val meterOptions = filteredMeters.map { "${it.number} (${it.type.displayName}) - ${it.address}" }
        meterAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, meterOptions)
        autoComplete.setAdapter(meterAdapter)
    }
    
    private fun setupAddressFilter(addressFilter: TextInputEditText, autoComplete: AutoCompleteTextView) {
        addressFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val filterText = s.toString().trim().lowercase()
                
                if (filterText.isEmpty()) {
                    // Если фильтр пустой, показываем все приборы
                    filteredMeters = meters
                } else {
                    // Фильтруем приборы по адресу
                    filteredMeters = meters.filter { meter ->
                        meter.address.lowercase().contains(filterText)
                    }
                }
                
                // Обновляем список приборов
                updateMeterDropdown(autoComplete)
                
                // Очищаем выбранный прибор, если он больше не в списке
                val currentSelection = autoComplete.text.toString()
                val isCurrentSelectionValid = filteredMeters.any { meter ->
                    "${meter.number} (${meter.type.displayName}) - ${meter.address}" == currentSelection
                }
                
                if (!isCurrentSelectionValid && currentSelection.isNotEmpty()) {
                    autoComplete.setText("")
                }
            }
        })
    }
    
    private fun setupMeterSelectionListener(dialogBinding: DialogAddReadingBinding) {
        dialogBinding.autoCompleteMeter.setOnItemClickListener { parent, view, position, id ->
            // Получаем выбранный прибор
            val selectedText = parent.getItemAtPosition(position).toString()
            val selectedMeter = filteredMeters.find { meter ->
                "${meter.number} (${meter.type.displayName}) - ${meter.address}" == selectedText
            }
            
            // Загружаем последнее показание для выбранного прибора
            selectedMeter?.let { meter ->
                loadLastReadingForMeter(meter.id, dialogBinding)
            }
        }
        
        // Также обновляем при изменении текста (если пользователь вводит вручную)
        dialogBinding.autoCompleteMeter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val selectedText = s.toString().trim()
                val selectedMeter = filteredMeters.find { meter ->
                    "${meter.number} (${meter.type.displayName}) - ${meter.address}" == selectedText
                }
                
                selectedMeter?.let { meter ->
                    loadLastReadingForMeter(meter.id, dialogBinding)
                } ?: run {
                    // Если прибор не выбран, скрываем карточку
                    hideLastReadingCard(dialogBinding)
                }
            }
        })
    }
    
    private fun loadLastReadingForMeter(meterId: Long, dialogBinding: DialogAddReadingBinding) {
        lifecycleScope.launch {
            try {
                val latestReading = repository.getLatestReadingByMeterId(meterId)
                
                withContext(Dispatchers.Main) {
                    if (latestReading != null) {
                        showLastReading(latestReading, dialogBinding)
                    } else {
                        hideLastReadingCard(dialogBinding)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ReadingsActivity", "Error loading last reading", e)
                withContext(Dispatchers.Main) {
                    hideLastReadingCard(dialogBinding)
                }
            }
        }
    }
    
    private fun showLastReading(reading: Reading, dialogBinding: DialogAddReadingBinding) {
        val cardLastReadings = dialogBinding.root.findViewById<androidx.cardview.widget.CardView>(R.id.cardLastReadings)
        val textLastValue = dialogBinding.root.findViewById<TextView>(R.id.textLastReadingValue)
        val textLastDate = dialogBinding.root.findViewById<TextView>(R.id.textLastReadingDate)
        
        cardLastReadings?.visibility = View.VISIBLE
        textLastValue?.text = String.format("%.2f", reading.value)
        textLastDate?.text = formatDateShort(reading.date)
    }
    
    private fun hideLastReadingCard(dialogBinding: DialogAddReadingBinding) {
        val cardLastReadings = dialogBinding.root.findViewById<androidx.cardview.widget.CardView>(R.id.cardLastReadings)
        cardLastReadings?.visibility = View.GONE
    }
    
    private fun formatDateShort(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
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
    
    private fun validateInput(binding: DialogAddReadingBinding): Boolean {
        val meterText = binding.autoCompleteMeter.text.toString().trim()
        val valueText = binding.editTextReadingValue.text.toString().trim()
        val dateText = binding.editTextReadingDate.text.toString().trim()
        
        if (meterText.isEmpty()) {
            binding.autoCompleteMeter.error = "Выберите прибор учета"
            return false
        }
        
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
        val meterText = binding.autoCompleteMeter.text.toString().trim()
        val value = binding.editTextReadingValue.text.toString().trim().toDoubleOrNull()
            ?: run {
                Toast.makeText(this, "Ошибка: некорректное значение показания", Toast.LENGTH_LONG).show()
                return
            }
        val dateText = binding.editTextReadingDate.text.toString().trim()
        
        // Находим выбранный прибор учета
        val selectedMeter = filteredMeters.find { meter ->
            "${meter.number} (${meter.type.displayName}) - ${meter.address}" == meterText
        }
        
        if (selectedMeter == null) {
            Toast.makeText(this, "Ошибка: прибор учета не найден", Toast.LENGTH_LONG).show()
            return
        }
        
        // Парсим дату и время
        val date = try {
            dateTimeFormat.parse(dateText)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
        
        val reading = Reading(
            meterId = selectedMeter.id,
            value = value,
            date = date
        )
        
        lifecycleScope.launch {
            try {
                repository.insertReading(reading)
                Toast.makeText(this@ReadingsActivity, "Показание добавлено", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ReadingsActivity, "Ошибка при добавлении: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showDeleteConfirmation(reading: Reading) {
        AlertDialog.Builder(this)
            .setTitle("Удаление показания")
            .setMessage("Вы уверены, что хотите удалить это показание?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteReading(reading)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun deleteReading(reading: Reading) {
        lifecycleScope.launch {
            try {
                repository.deleteReading(reading)
                Toast.makeText(this@ReadingsActivity, "Показание удалено", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@ReadingsActivity, "Ошибка при удалении: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}