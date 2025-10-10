package com.worldclock.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.worldclock.app.data.*
import kotlinx.coroutines.runBlocking

object TestUtils {
    
    fun createTestDatabase(): AppDatabase {
        return Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }
    
    fun createTestMeter(
        id: Long = 0L,
        number: String = "TEST-001",
        address: String = "ул. Тестовая, д. 1",
        type: MeterType = MeterType.ELECTRICITY
    ): Meter {
        return Meter(id, number, address, type)
    }
    
    fun createTestReading(
        id: Long = 0L,
        meterId: Long = 1L,
        value: Double = 1000.0,
        date: Long = System.currentTimeMillis()
    ): Reading {
        return Reading(id, meterId, value, date)
    }
    
    fun createTestTariff(
        id: Long = 0L,
        meterId: Long = 1L,
        rate: Double = 4.5,
        startDate: Long = System.currentTimeMillis(),
        endDate: Long? = null
    ): Tariff {
        return Tariff(id, meterId, rate, startDate, endDate)
    }
    
    fun populateTestData(database: AppDatabase) = runBlocking {
        val meterDao = database.meterDao()
        val readingDao = database.readingDao()
        val tariffDao = database.tariffDao()
        
        // Create test meters
        val meter1Id = meterDao.insertMeter(createTestMeter(0L, "EL-001", "ул. Ленина, д. 10", MeterType.ELECTRICITY))
        val meter2Id = meterDao.insertMeter(createTestMeter(0L, "GAS-002", "ул. Пушкина, д. 5", MeterType.GAS))
        
        // Create test readings
        val currentTime = System.currentTimeMillis()
        val previousTime = currentTime - 86400000L // 1 day ago
        
        readingDao.insertReading(createTestReading(0L, meter1Id, 1200.0, previousTime))
        readingDao.insertReading(createTestReading(0L, meter1Id, 1250.0, currentTime))
        readingDao.insertReading(createTestReading(0L, meter2Id, 800.0, previousTime))
        readingDao.insertReading(createTestReading(0L, meter2Id, 850.0, currentTime))
        
        // Create test tariffs
        tariffDao.insertTariff(createTestTariff(0L, meter1Id, 4.5, previousTime, null))
        tariffDao.insertTariff(createTestTariff(0L, meter2Id, 6.2, previousTime, null))
    }
    
    fun clearTestData(database: AppDatabase) = runBlocking {
        database.clearAllTables()
    }
}