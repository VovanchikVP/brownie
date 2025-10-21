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
import com.worldclock.app.tutorial.TutorialManager
import com.worldclock.app.ui.AddressCostAdapter
import com.worldclock.app.ui.AddressCostItem
import com.worldclock.app.ui.CostItem
import kotlinx.coroutines.launch

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
    
    
    private fun loadCosts() {
        lifecycleScope.launch {
            try {
                val meters = repository.getAllMeters()
                meters.collect { meterList ->
                    // Обновляем состояние наличия приборов
                    hasMeters = meterList.isNotEmpty()
                    
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
    
    override fun onDestroy() {
        super.onDestroy()
        tutorialManager.hideTutorial()
    }
}