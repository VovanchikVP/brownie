package com.worldclock.app

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.worldclock.app.data.*
import com.worldclock.app.databinding.ActivityMainBinding
import com.worldclock.app.databinding.DialogAddReadingBinding
import com.worldclock.app.databinding.DialogMenuBinding
import com.worldclock.app.tutorial.TutorialManager
import com.worldclock.app.ui.AddressCostAdapter
import com.worldclock.app.ui.AddressCostItem
import com.worldclock.app.ui.CostItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    // Database для управления приборами учета
    private lateinit var database: AppDatabase
    private lateinit var repository: MeterRepository
    private lateinit var addressCostAdapter: AddressCostAdapter
    
    // Tutorial manager
    private lateinit var tutorialManager: TutorialManager

    // Состояние для отслеживания наличия приборов
    private var hasMeters = false
    
    // Для диалога добавления показаний
    private var meters: List<Meter> = emptyList()
    private var filteredMeters: List<Meter> = emptyList()
    private var meterAdapter: ArrayAdapter<String>? = null
    
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Инициализация базы данных
        initializeDatabase()
        
        // Инициализация tutorial manager
        tutorialManager = TutorialManager(this)

        // Настройка RecyclerView для затрат
        setupCostsRecyclerView()
        
        // Настройка обработчиков
        setupClickListeners()
        
        // Загружаем затраты
        loadCosts()
        
        // Проверяем, нужно ли показать обучение
        checkTutorial()
    }
    
    private fun initializeDatabase() {
        database = AppDatabase.getDatabase(this)
        repository = MeterRepository(
            database.meterDao(),
            database.readingDao(),
            database.tariffDao()
        )
        
        // База данных инициализирована без тестовых данных
    }
    
    private fun setupCostsRecyclerView() {
        addressCostAdapter = AddressCostAdapter { addressCostItem ->
            // Переходим к экрану детализации затрат по адресу
            val intent = Intent(this, AddressCostsActivity::class.java).apply {
                putExtra("address", addressCostItem.address)
            }
            startActivity(intent)
        }
        binding.recyclerViewCosts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCosts.adapter = addressCostAdapter
    }
    
    private fun setupClickListeners() {
        binding.menuButton.setOnClickListener {
            showMenu()
        }
        
        binding.fabAddReading.setOnClickListener {
            showAddReadingDialog()
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
        
        dialogBinding.menuItemData.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, DataActivity::class.java)
            startActivity(intent)
        }
        
        dialog.show()
    }
    
    
    private fun loadCosts() {
        lifecycleScope.launch {
            try {
                    val meters = repository.getAllMeters()
                    meters.collect { meterList ->
                        // Обновляем состояние наличия приборов
                        hasMeters = meterList.isNotEmpty()
                        
                        // Сохраняем список приборов для диалога
                        this@MainActivity.meters = meterList
                    
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
                    
                    // Группируем по адресам
                    val addressCosts = groupCostsByAddress(costItems)
                    
                    // Обновляем UI
                    updateNoMetersNotification()
                    addressCostAdapter.updateAddressCosts(addressCosts)
                    updateEmptyState(addressCosts.isEmpty())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun groupCostsByAddress(costItems: List<CostItem>): List<AddressCostItem> {
        val groupedByAddress = costItems.groupBy { it.meterAddress }
        
        return groupedByAddress.map { (address, costs) ->
            val totalCost = costs.sumOf { it.cost }
            AddressCostItem(
                address = address,
                totalCost = totalCost,
                metersCount = costs.size,
                meters = costs
            )
        }.sortedByDescending { it.totalCost }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        // Если нет приборов, показываем уведомление в RecyclerView, а не пустое состояние
        if (!hasMeters) {
            binding.emptyStateText.visibility = View.GONE
            binding.recyclerViewCosts.visibility = View.VISIBLE
        } else {
            binding.emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerViewCosts.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }
    
    private fun updateNoMetersNotification() {
        if (!hasMeters) {
            showNoMetersNotification()
        } else {
            hideNoMetersNotification()
        }
    }
    
    private fun showNoMetersNotification() {
        // Создаем уведомление без parent для полного контроля над layout
        val notificationView = LayoutInflater.from(this).inflate(R.layout.item_no_meters_full_width, null)
        
        // Настраиваем обработчик для кнопки "Открыть меню"
        val buttonOpenMenu = notificationView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonOpenMenu)
        buttonOpenMenu.setOnClickListener {
            // Переходим на экран управления приборами и сразу открываем диалог добавления
            val intent = Intent(this, MetersActivity::class.java).apply {
                putExtra("open_add_dialog", true)
            }
            startActivity(intent)
        }
        
        // Добавляем уведомление в RecyclerView
        addressCostAdapter.addNotificationItem(notificationView)
    }
    
    private fun hideNoMetersNotification() {
        // Убираем уведомление
        addressCostAdapter.removeNotificationItem()
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем затраты при возвращении на экран
        loadCosts()
    }
    
    private fun checkTutorial() {
        if (!tutorialManager.isTutorialCompleted()) {
            // Небольшая задержка, чтобы UI успел загрузиться
            binding.root.post {
                tutorialManager.startTutorial()
            }
        }
    }

    private fun showAddReadingDialog() {
        val dialogBinding = DialogAddReadingBinding.inflate(LayoutInflater.from(this))
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        setupMeterDropdown(dialogBinding.autoCompleteMeter)
        setupAddressFilter(dialogBinding.editTextAddressFilter, dialogBinding.autoCompleteMeter)
        
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
        val value = binding.editTextReadingValue.text.toString().trim().toDouble()
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
                Toast.makeText(this@MainActivity, "Показание добавлено", Toast.LENGTH_SHORT).show()
                // Обновляем затраты после добавления показания
                loadCosts()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка при добавлении: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tutorialManager.hideTutorial()
    }
}