package com.worldclock.app

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.worldclock.app.data.*
import com.worldclock.app.databinding.ActivityMetersBinding
import com.worldclock.app.databinding.DialogAddMeterBinding
import com.worldclock.app.ui.MeterAdapter
import kotlinx.coroutines.launch

class MetersActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMetersBinding
    private lateinit var database: AppDatabase
    private lateinit var repository: MeterRepository
    private lateinit var adapter: MeterAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMetersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeDatabase()
        setupRecyclerView()
        setupClickListeners()
        loadMeters()
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
        
        binding.appNameText.setOnClickListener {
            // Возвращаемся на главную страницу
            finish()
        }
    }
    
    private fun loadMeters() {
        lifecycleScope.launch {
            repository.getAllMeters().collect { meters ->
                adapter.updateMeters(meters)
                updateEmptyState(meters.isEmpty())
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
}