package com.worldclock.app

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.worldclock.app.data.*
import com.worldclock.app.databinding.ActivityDataBinding
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class DataActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDataBinding
    private lateinit var database: AppDatabase
    private lateinit var repository: MeterRepository
    
    // Activity Result Launcher для импорта файлов
    private val importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            result.data?.data?.let { uri ->
                loadFromFile(uri)
            }
        }
    }
    
    companion object {
        private const val REQUEST_CODE_EXPORT = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Инициализация базы данных
        initializeDatabase()
        
        // Настройка обработчиков
        setupClickListeners()
    }
    
    private fun initializeDatabase() {
        database = AppDatabase.getDatabase(this)
        repository = MeterRepository(
            database.meterDao(),
            database.readingDao(),
            database.tariffDao()
        )
    }
    
    private fun setupClickListeners() {
        binding.imageViewBackButton.setOnClickListener {
            finish()
        }
        
        binding.buttonExportData.setOnClickListener {
            lifecycleScope.launch {
                try {
                    // Получаем данные из Flow
                    var metersList: List<Meter> = emptyList()
                    var readingsList: List<Reading> = emptyList()
                    var tariffsList: List<Tariff> = emptyList()
                    
                    repository.getAllMeters().collect { metersList = it }
                    repository.getAllReadings().collect { readingsList = it }
                    repository.getAllTariffs().collect { tariffsList = it }
                    
                    val jsonObject = JSONObject().apply {
                        put("version", "1.0")
                        put("exportDate", System.currentTimeMillis())
                        
                        val metersArray = JSONArray()
                        metersList.forEach { meter ->
                            val meterJson = JSONObject().apply {
                                put("id", meter.id)
                                put("number", meter.number)
                                put("address", meter.address)
                                put("type", meter.type.name)
                            }
                            metersArray.put(meterJson)
                        }
                        put("meters", metersArray)
                        
                        val readingsArray = JSONArray()
                        readingsList.forEach { reading ->
                            val readingJson = JSONObject().apply {
                                put("id", reading.id)
                                put("meterId", reading.meterId)
                                put("value", reading.value)
                                put("date", reading.date)
                            }
                            readingsArray.put(readingJson)
                        }
                        put("readings", readingsArray)
                        
                        val tariffsArray = JSONArray()
                        tariffsList.forEach { tariff ->
                            val tariffJson = JSONObject().apply {
                                put("id", tariff.id)
                                put("meterId", tariff.meterId)
                                put("rate", tariff.rate)
                                put("startDate", tariff.startDate)
                                put("endDate", tariff.endDate ?: JSONObject.NULL)
                            }
                            tariffsArray.put(tariffJson)
                        }
                        put("tariffs", tariffsArray)
                    }
                    
                    saveToFile(jsonObject.toString())
                } catch (e: Exception) {
                    Toast.makeText(this@DataActivity, "Ошибка при экспорте данных: ${e.message ?: "Неизвестная ошибка"}", Toast.LENGTH_LONG).show()
                }
            }
        }
        
        binding.buttonImportData.setOnClickListener {
            importData()
        }
    }
    
    private fun exportData() {
        lifecycleScope.launch {
            try {
                // Получаем все данные
                val meters = repository.getAllMeters()
                val readings = repository.getAllReadings()
                val tariffs = repository.getAllTariffs()
                
                // Создаем JSON объект с данными
                val exportData = JSONObject().apply {
                    put("version", "1.0")
                    put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                    
                    // Экспортируем приборы
                    val metersArray = JSONArray()
                    meters.collect { meterList ->
                        meterList.forEach { meter ->
                            val meterJson = JSONObject().apply {
                                put("id", meter.id)
                                put("number", meter.number)
                                put("address", meter.address)
                                put("type", meter.type.name)
                            }
                            metersArray.put(meterJson)
                        }
                        
                        // Экспортируем показания
                        val readingsArray = JSONArray()
                        readings.collect { readingList ->
                            readingList.forEach { reading ->
                                val readingJson = JSONObject().apply {
                                    put("id", reading.id)
                                    put("meterId", reading.meterId)
                                    put("value", reading.value)
                                    put("date", reading.date)
                                }
                                readingsArray.put(readingJson)
                            }
                            
                            // Экспортируем тарифы
                            val tariffsArray = JSONArray()
                            tariffs.collect { tariffList ->
                                tariffList.forEach { tariff ->
                                    val tariffJson = JSONObject().apply {
                                        put("id", tariff.id)
                                        put("meterId", tariff.meterId)
                                        put("rate", tariff.rate)
                                        put("startDate", tariff.startDate)
                                        put("endDate", tariff.endDate ?: JSONObject.NULL)
                                    }
                                    tariffsArray.put(tariffJson)
                                }
                                
                                // Завершаем создание JSON
                                put("meters", metersArray)
                                put("readings", readingsArray)
                                put("tariffs", tariffsArray)
                            }
                        }
                    }
                }
                
                // Сохраняем в файл
                saveToFile(exportData.toString())
            } catch (e: Exception) {
                Toast.makeText(this@DataActivity, "Ошибка при экспорте: ${e.message ?: "Неизвестная ошибка"}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun saveToFile(jsonData: String) {
        try {
            val fileName = "meters_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
            val file = File(getExternalFilesDir(null), fileName)
            
            file.writeText(jsonData)
            
            // Показываем диалог с результатом
            AlertDialog.Builder(this)
                .setTitle("Экспорт завершен")
                .setMessage("Данные сохранены в файл:\n${file.name}\n\nПуть: ${file.absolutePath}")
                .setPositiveButton("OK", null)
                .show()
                
            Toast.makeText(this, "Данные успешно экспортированы", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при сохранении файла: ${e.message ?: "Неизвестная ошибка"}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun importData() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        importLauncher.launch(intent)
    }
    
    
    private fun loadFromFile(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonData = inputStream.bufferedReader().use { it.readText() }
                parseAndImportData(jsonData)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при чтении файла: ${e.message ?: "Неизвестная ошибка"}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun parseAndImportData(jsonData: String) {
        lifecycleScope.launch {
            try {
                val jsonObject = JSONObject(jsonData)
                
                // Проверяем версию
                val version = jsonObject.getString("version")
                if (version != "1.0") {
                    Toast.makeText(this@DataActivity, "Неподдерживаемая версия файла", Toast.LENGTH_LONG).show()
                    return@launch
                }
                
                // Показываем диалог подтверждения
                AlertDialog.Builder(this@DataActivity)
                    .setTitle("Импорт данных")
                    .setMessage("Внимание! Импорт данных заменит все существующие данные. Продолжить?")
                    .setPositiveButton("Да") { _, _ ->
                        performImport(jsonObject)
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
                    
            } catch (e: Exception) {
                Toast.makeText(this@DataActivity, "Ошибка при парсинге файла: ${e.message ?: "Неизвестная ошибка"}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun performImport(jsonObject: JSONObject) {
        lifecycleScope.launch {
            try {
                // Очищаем существующие данные
                repository.deleteAllTariffs()
                repository.deleteAllReadings()
                repository.deleteAllMeters()
                
                // Импортируем приборы
                val metersArray = jsonObject.getJSONArray("meters")
                val meterIdMap = mutableMapOf<Long, Long>() // старое ID -> новое ID
                
                for (i in 0 until metersArray.length()) {
                    val meterJson = metersArray.getJSONObject(i)
                    val oldId = meterJson.getLong("id")
                    
                    val meter = Meter(
                        number = meterJson.getString("number"),
                        address = meterJson.getString("address"),
                        type = MeterType.valueOf(meterJson.getString("type"))
                    )
                    
                    val newId = repository.insertMeter(meter)
                    meterIdMap[oldId] = newId
                }
                
                // Импортируем показания
                val readingsArray = jsonObject.getJSONArray("readings")
                for (i in 0 until readingsArray.length()) {
                    val readingJson = readingsArray.getJSONObject(i)
                    val oldMeterId = readingJson.getLong("meterId")
                    val newMeterId = meterIdMap[oldMeterId] ?: continue
                    
                    val reading = Reading(
                        meterId = newMeterId,
                        value = readingJson.getDouble("value"),
                        date = readingJson.getLong("date")
                    )
                    
                    repository.insertReading(reading)
                }
                
                // Импортируем тарифы
                val tariffsArray = jsonObject.getJSONArray("tariffs")
                for (i in 0 until tariffsArray.length()) {
                    val tariffJson = tariffsArray.getJSONObject(i)
                    val oldMeterId = tariffJson.getLong("meterId")
                    val newMeterId = meterIdMap[oldMeterId] ?: continue
                    
                    val tariff = Tariff(
                        meterId = newMeterId,
                        rate = tariffJson.getDouble("rate"),
                        startDate = tariffJson.getLong("startDate"),
                        endDate = if (tariffJson.isNull("endDate")) null else tariffJson.getLong("endDate")
                    )
                    
                    repository.insertTariff(tariff)
                }
                
                Toast.makeText(this@DataActivity, "Данные успешно импортированы", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@DataActivity, "Ошибка при импорте: ${e.message ?: "Неизвестная ошибка"}", Toast.LENGTH_LONG).show()
            }
        }
    }
}