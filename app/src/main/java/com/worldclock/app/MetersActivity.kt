package com.worldclock.app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RadioButton
import android.widget.Toast
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.worldclock.app.data.*
import com.worldclock.app.databinding.ActivityMetersBinding
import com.worldclock.app.databinding.DialogAddMeterBinding
import com.worldclock.app.databinding.DialogAddTariffBinding
import com.worldclock.app.databinding.DialogAddTariffSuggestionBinding
import com.worldclock.app.tutorial.TutorialManager
import com.worldclock.app.ui.MeterAdapter
import kotlinx.coroutines.launch

class MetersActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMetersBinding
    private lateinit var database: AppDatabase
    private lateinit var repository: MeterRepository
    private lateinit var adapter: MeterAdapter
    private var meters: List<Meter> = emptyList()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private lateinit var tutorialManager: TutorialManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMetersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeDatabase()
        tutorialManager = TutorialManager(this)
        setupRecyclerView()
        setupClickListeners()
        loadMeters()
        
        // Проверяем, нужно ли автоматически открыть диалог добавления прибора
        val shouldOpenAddDialog = intent.getBooleanExtra("open_add_dialog", false)
        if (shouldOpenAddDialog) {
            // Небольшая задержка, чтобы UI успел загрузиться
            binding.root.post {
                showAddMeterDialog()
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
        adapter = MeterAdapter(
            meters = emptyList(),
            onDeleteClick = { meter -> showDeleteConfirmation(meter) }
        )
        
        binding.recyclerViewMeters.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMeters.adapter = adapter
    }
    
    private fun setupClickListeners() {
        binding.fabAddMeter.setOnClickListener {
            showAddMeterDialog()
        }
        
        binding.imageViewBackButton.setOnClickListener {
            // Возвращаемся на главную страницу
            finish()
        }
    }
    
    private fun loadMeters() {
        lifecycleScope.launch {
            repository.getAllMeters().collect { meterList ->
                meters = meterList
                adapter.updateMeters(meterList)
                updateEmptyState(meterList.isEmpty())
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewMeters.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun showAddMeterDialog() {
        val dialogBinding = DialogAddMeterBinding.inflate(LayoutInflater.from(this))
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.buttonSave.setOnClickListener {
            if (validateInput(dialogBinding)) {
                saveMeter(dialogBinding)
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    private fun validateInput(binding: DialogAddMeterBinding): Boolean {
        val number = binding.editTextMeterNumber.text.toString().trim()
        val address = binding.editTextMeterAddress.text.toString().trim()
        
        if (number.isEmpty()) {
            binding.editTextMeterNumber.error = "Введите номер прибора"
            return false
        }
        
        if (address.isEmpty()) {
            binding.editTextMeterAddress.error = "Введите адрес установки"
            return false
        }
        
        return true
    }
    
    private fun saveMeter(binding: DialogAddMeterBinding) {
        val number = binding.editTextMeterNumber.text.toString().trim()
        val address = binding.editTextMeterAddress.text.toString().trim()
        
        val meterType = when (binding.radioGroupMeterType.checkedRadioButtonId) {
            R.id.radioElectricity -> MeterType.ELECTRICITY
            R.id.radioGas -> MeterType.GAS
            R.id.radioHotWater -> MeterType.HOT_WATER
            R.id.radioColdWater -> MeterType.COLD_WATER
            else -> MeterType.ELECTRICITY
        }
        
        val meter = Meter(
            number = number,
            address = address,
            type = meterType
        )
        
        lifecycleScope.launch {
            try {
                repository.insertMeter(meter)
                Toast.makeText(this@MetersActivity, "Прибор учета добавлен", Toast.LENGTH_SHORT).show()
                
                // Проверяем, нужно ли продолжить обучение
                if (tutorialManager.getCurrentStep() == TutorialManager.STEP_ADD_METER) {
                    tutorialManager.nextStep()
                }
                
                // Показываем предложение добавить тариф
                showAddTariffSuggestion(meter)
            } catch (e: Exception) {
                Toast.makeText(this@MetersActivity, "Ошибка при добавлении: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showDeleteConfirmation(meter: Meter) {
        AlertDialog.Builder(this)
            .setTitle("Удаление прибора")
            .setMessage("Вы уверены, что хотите удалить прибор учета \"${meter.number}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteMeter(meter)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun deleteMeter(meter: Meter) {
        lifecycleScope.launch {
            try {
                repository.deleteMeter(meter)
                Toast.makeText(this@MetersActivity, "Прибор учета удален", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MetersActivity, "Ошибка при удалении: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showAddTariffSuggestion(meter: Meter) {
        val dialogBinding = DialogAddTariffSuggestionBinding.inflate(LayoutInflater.from(this))
        
        // Устанавливаем информацию о приборе
        val meterInfo = "${meter.number} (${meter.type.displayName})"
        dialogBinding.textMeterInfo.text = meterInfo
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        
        dialogBinding.buttonLater.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.buttonAddTariff.setOnClickListener {
            dialog.dismiss()
            // Показываем диалог добавления тарифа с предзаполненным прибором
            showAddTariffDialog(meter)
        }
        
        dialogBinding.buttonLater.setOnClickListener {
            dialog.dismiss()
            // Если это обучение, переходим к следующему шагу
            if (tutorialManager.getCurrentStep() == TutorialManager.STEP_ADD_TARIFF) {
                tutorialManager.nextStep()
            }
        }
        
        dialog.show()
    }
    
    private fun showAddTariffDialog(preSelectedMeter: Meter) {
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
            if (validateTariffInput(dialogBinding)) {
                saveTariff(dialogBinding)
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    private fun setupMeterDropdown(autoComplete: AutoCompleteTextView, preSelectedMeter: Meter) {
        val meterOptions = meters.map { "${it.number} (${it.type.displayName})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, meterOptions)
        autoComplete.setAdapter(adapter)
        
        // Устанавливаем предзаполненный прибор
        val selectedText = "${preSelectedMeter.number} (${preSelectedMeter.type.displayName})"
        autoComplete.setText(selectedText, false)
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
            if (!isChecked) {
                binding.editTextEndDate.setText("")
            }
        }
    }
    
    private fun validateTariffInput(binding: DialogAddTariffBinding): Boolean {
        val rateText = binding.editTextTariffRate.text.toString().trim()
        val startDateText = binding.editTextStartDate.text.toString().trim()
        val endDateText = binding.editTextEndDate.text.toString().trim()
        
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
            binding.editTextStartDate.error = "Введите дату начала действия"
            return false
        }
        
        if (binding.checkboxEndDate.isChecked && endDateText.isEmpty()) {
            binding.editTextEndDate.error = "Введите дату окончания действия"
            return false
        }
        
        return true
    }
    
    private fun saveTariff(binding: DialogAddTariffBinding) {
        val rateText = binding.editTextTariffRate.text.toString().trim()
        val startDateText = binding.editTextStartDate.text.toString().trim()
        val endDateText = binding.editTextEndDate.text.toString().trim()
        val meterText = binding.autoCompleteMeter.text.toString().trim()
        
        val rate = rateText.toDouble()
        val startDate = dateFormat.parse(startDateText)?.time ?: System.currentTimeMillis()
        val endDate = if (binding.checkboxEndDate.isChecked && endDateText.isNotEmpty()) {
            dateFormat.parse(endDateText)?.time
        } else null
        
        // Находим прибор по тексту
        val meter = meters.find { 
            "${it.number} (${it.type.displayName})" == meterText 
        }
        
        if (meter == null) {
            Toast.makeText(this, "Прибор не найден", Toast.LENGTH_SHORT).show()
            return
        }
        
        val tariff = Tariff(
            meterId = meter.id,
            rate = rate,
            startDate = startDate,
            endDate = endDate
        )
        
        lifecycleScope.launch {
            try {
                repository.insertTariff(tariff)
                Toast.makeText(this@MetersActivity, "Тариф добавлен", Toast.LENGTH_SHORT).show()
                
                // Если это обучение, переходим к следующему шагу
                if (tutorialManager.getCurrentStep() == TutorialManager.STEP_ADD_TARIFF) {
                    tutorialManager.nextStep()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MetersActivity, "Ошибка при добавлении тарифа: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        tutorialManager.hideTutorial()
    }
}