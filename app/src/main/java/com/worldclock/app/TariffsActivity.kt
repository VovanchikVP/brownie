package com.worldclock.app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.worldclock.app.data.*
import com.worldclock.app.databinding.ActivityTariffsBinding
import com.worldclock.app.databinding.DialogAddTariffBinding
import com.worldclock.app.databinding.DialogEditTariffEndDateBinding
import com.worldclock.app.ui.TariffAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TariffsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTariffsBinding
    private lateinit var database: AppDatabase
    private lateinit var repository: MeterRepository
    private lateinit var adapter: TariffAdapter
    private var meters: List<Meter> = emptyList()
    
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTariffsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeDatabase()
        setupRecyclerView()
        setupClickListeners()
        loadTariffs()
        
        // Проверяем, пришли ли параметры от MetersActivity
        val meterId = intent.getLongExtra("meterId", -1L)
        val meterNumber = intent.getStringExtra("meterNumber")
        
        if (meterId != -1L && meterNumber != null) {
            // Показываем диалог добавления тарифа с предзаполненным прибором
            lifecycleScope.launch {
                try {
                    // Загружаем приборы напрямую
                    val meterList = repository.getAllMetersSync()
                    meters = meterList
                    val meter = meterList.find { it.id == meterId }
                    if (meter != null) {
                        showAddTariffDialog(meter)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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
        adapter = TariffAdapter(
            tariffs = emptyList(),
            onDeleteClick = { tariff -> showDeleteConfirmation(tariff) },
            onItemClick = { tariff -> showEditTariffEndDateDialog(tariff) }
        )
        
        binding.recyclerViewTariffs.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewTariffs.adapter = adapter
    }
    
    private fun setupClickListeners() {
        binding.fabAddTariff.setOnClickListener {
            showAddTariffDialog()
        }
        
        binding.imageViewBackButton.setOnClickListener {
            // Возвращаемся на главную страницу
            finish()
        }
    }
    
    private fun loadTariffs() {
        lifecycleScope.launch {
            try {
                // Загружаем приборы учета
                repository.getAllMeters().collect { metersList ->
                    meters = metersList
                    
                    // Загружаем тарифы
                    repository.getAllTariffs().collect { tariffs ->
                        val tariffsWithMeters = tariffs.map { tariff ->
                            val meter = meters.find { it.id == tariff.meterId }
                            TariffAdapter.TariffWithMeter(
                                tariff = tariff,
                                meterNumber = meter?.number ?: "Неизвестно",
                                meterType = meter?.type?.displayName ?: "Неизвестно"
                            )
                        }
                        
                        adapter.updateTariffs(tariffsWithMeters)
                        updateEmptyState(tariffsWithMeters.isEmpty())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewTariffs.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun showAddTariffDialog() {
        showAddTariffDialog(null)
    }
    
    private fun showAddTariffDialog(preSelectedMeter: Meter?) {
        val dialogBinding = DialogAddTariffBinding.inflate(LayoutInflater.from(this))
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        setupMeterDropdown(dialogBinding.autoCompleteMeter, preSelectedMeter)
        setupDatePickers(dialogBinding, dialog)
        setupCheckboxListener(dialogBinding)
        
        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.buttonSave.setOnClickListener {
            if (validateInput(dialogBinding)) {
                saveTariff(dialogBinding)
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    private fun setupMeterDropdown(autoComplete: AutoCompleteTextView, preSelectedMeter: Meter? = null) {
        val meterOptions = meters.map { "${it.number} (${it.type.displayName})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, meterOptions)
        autoComplete.setAdapter(adapter)
        
        // Если передан предзаполненный прибор, устанавливаем его
        if (preSelectedMeter != null) {
            val selectedText = "${preSelectedMeter.number} (${preSelectedMeter.type.displayName})"
            autoComplete.setText(selectedText, false)
        }
    }
    
    @Suppress("UNUSED_PARAMETER")
    private fun setupDatePickers(binding: DialogAddTariffBinding, dialog: AlertDialog) {
        val calendar = Calendar.getInstance()
        
        binding.editTextStartDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    binding.editTextStartDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        
        binding.editTextEndDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    binding.editTextEndDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
    
    private fun setupCheckboxListener(binding: DialogAddTariffBinding) {
        binding.checkboxEndDate.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutEndDate.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }
    
    private fun validateInput(binding: DialogAddTariffBinding): Boolean {
        val meterText = binding.autoCompleteMeter.text.toString().trim()
        val rateText = binding.editTextTariffRate.text.toString().trim()
        val startDateText = binding.editTextStartDate.text.toString().trim()
        
        if (meterText.isEmpty()) {
            binding.autoCompleteMeter.error = "Выберите прибор учета"
            return false
        }
        
        if (rateText.isEmpty()) {
            binding.editTextTariffRate.error = "Введите тариф"
            return false
        }
        
        val rate = rateText.toDoubleOrNull()
        if (rate == null || rate <= 0) {
            binding.editTextTariffRate.error = "Введите корректный тариф"
            return false
        }
        
        if (startDateText.isEmpty()) {
            binding.editTextStartDate.error = "Выберите дату начала"
            return false
        }
        
        if (binding.checkboxEndDate.isChecked && binding.editTextEndDate.text.toString().trim().isEmpty()) {
            binding.editTextEndDate.error = "Выберите дату окончания"
            return false
        }
        
        return true
    }
    
    private fun saveTariff(binding: DialogAddTariffBinding) {
        val meterText = binding.autoCompleteMeter.text.toString().trim()
        val rate = binding.editTextTariffRate.text.toString().trim().toDouble()
        val startDateText = binding.editTextStartDate.text.toString().trim()
        val endDateText = binding.editTextEndDate.text.toString().trim()
        
        // Находим выбранный прибор учета
        val selectedMeter = meters.find { meter ->
            "${meter.number} (${meter.type.displayName})" == meterText
        }
        
        if (selectedMeter == null) {
            Toast.makeText(this, "Ошибка: прибор учета не найден", Toast.LENGTH_LONG).show()
            return
        }
        
        // Парсим даты
        val startDate = try {
            dateFormat.parse(startDateText)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
        
        val endDate = if (binding.checkboxEndDate.isChecked && endDateText.isNotEmpty()) {
            try {
                dateFormat.parse(endDateText)?.time
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
        
        val tariff = Tariff(
            meterId = selectedMeter.id,
            rate = rate,
            startDate = startDate,
            endDate = endDate
        )
        
        lifecycleScope.launch {
            try {
                repository.insertTariff(tariff)
                Toast.makeText(this@TariffsActivity, "Тариф добавлен", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@TariffsActivity, "Ошибка при добавлении: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showDeleteConfirmation(tariff: Tariff) {
        AlertDialog.Builder(this)
            .setTitle("Удаление тарифа")
            .setMessage("Вы уверены, что хотите удалить этот тариф?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteTariff(tariff)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun deleteTariff(tariff: Tariff) {
        lifecycleScope.launch {
            try {
                repository.deleteTariff(tariff)
                Toast.makeText(this@TariffsActivity, "Тариф удален", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@TariffsActivity, "Ошибка при удалении: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showEditTariffEndDateDialog(tariff: Tariff) {
        val dialogBinding = DialogEditTariffEndDateBinding.inflate(LayoutInflater.from(this))
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        // Находим информацию о приборе учета
        val meter = meters.find { it.id == tariff.meterId }
        val meterInfo = if (meter != null) {
            "${meter.number} - ${meter.type.displayName}"
        } else {
            "Неизвестный прибор"
        }
        
        // Заполняем информацию о тарифе
        dialogBinding.textViewTariffInfo.text = meterInfo
        dialogBinding.textViewCurrentRate.text = "${tariff.rate} ₽"
        dialogBinding.textViewStartDate.text = dateFormat.format(Date(tariff.startDate))
        
        // Устанавливаем текущую дату окончания, если есть
        if (tariff.endDate != null) {
            dialogBinding.editTextEndDate.setText(dateFormat.format(Date(tariff.endDate)))
        }
        
        // Настраиваем выбор даты окончания
        setupEndDatePicker(dialogBinding.editTextEndDate)
        
        // Настраиваем чекбокс для удаления даты окончания
        setupRemoveEndDateCheckbox(dialogBinding)
        
        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.buttonSave.setOnClickListener {
            if (validateEditInput(dialogBinding)) {
                saveTariffEndDate(tariff, dialogBinding)
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    private fun setupEndDatePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        
        editText.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    editText.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
    
    private fun setupRemoveEndDateCheckbox(binding: DialogEditTariffEndDateBinding) {
        binding.checkboxRemoveEndDate.setOnCheckedChangeListener { _, isChecked ->
            binding.editTextEndDate.isEnabled = !isChecked
            if (isChecked) {
                binding.editTextEndDate.setText("")
            }
        }
    }
    
    private fun validateEditInput(binding: DialogEditTariffEndDateBinding): Boolean {
        val endDateText = binding.editTextEndDate.text.toString().trim()
        val removeEndDate = binding.checkboxRemoveEndDate.isChecked
        
        if (!removeEndDate && endDateText.isEmpty()) {
            binding.editTextEndDate.error = "Выберите дату окончания или отметьте чекбокс"
            return false
        }
        
        return true
    }
    
    private fun saveTariffEndDate(tariff: Tariff, binding: DialogEditTariffEndDateBinding) {
        val endDateText = binding.editTextEndDate.text.toString().trim()
        val removeEndDate = binding.checkboxRemoveEndDate.isChecked
        
        val endDate = if (removeEndDate) {
            null
        } else {
            try {
                dateFormat.parse(endDateText)?.time
            } catch (e: Exception) {
                null
            }
        }
        
        val updatedTariff = tariff.copy(endDate = endDate)
        
        lifecycleScope.launch {
            try {
                repository.updateTariff(updatedTariff)
                Toast.makeText(this@TariffsActivity, "Тариф обновлен", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@TariffsActivity, "Ошибка при обновлении: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}