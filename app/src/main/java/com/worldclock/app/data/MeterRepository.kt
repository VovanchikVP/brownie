package com.worldclock.app.data

import kotlinx.coroutines.flow.Flow

class MeterRepository(
    private val meterDao: MeterDao,
    private val readingDao: ReadingDao,
    private val tariffDao: TariffDao
) {
    
    // Meters
    fun getAllMeters(): Flow<List<Meter>> = meterDao.getAllMeters()
    
    suspend fun getMeterById(id: Long): Meter? = meterDao.getMeterById(id)
    
    fun getMetersByType(type: MeterType): Flow<List<Meter>> = meterDao.getMetersByType(type)
    
    suspend fun insertMeter(meter: Meter): Long = meterDao.insertMeter(meter)
    
    suspend fun updateMeter(meter: Meter) = meterDao.updateMeter(meter)
    
    suspend fun deleteMeter(meter: Meter) = meterDao.deleteMeter(meter)
    
    // Readings
    fun getAllReadings(): Flow<List<Reading>> = readingDao.getAllReadings()
    
    fun getReadingsByMeterId(meterId: Long): Flow<List<Reading>> = readingDao.getReadingsByMeterId(meterId)
    
    suspend fun getLatestReadingByMeterId(meterId: Long): Reading? = readingDao.getLatestReadingByMeterId(meterId)
    
    suspend fun getPreviousReadingByMeterId(meterId: Long): Reading? = readingDao.getPreviousReadingByMeterId(meterId)
    
    suspend fun insertReading(reading: Reading): Long = readingDao.insertReading(reading)
    
    suspend fun updateReading(reading: Reading) = readingDao.updateReading(reading)
    
    suspend fun deleteReading(reading: Reading) = readingDao.deleteReading(reading)
    
    // Tariffs
    fun getAllTariffs(): Flow<List<Tariff>> = tariffDao.getAllTariffs()
    
    fun getTariffsByMeterId(meterId: Long): Flow<List<Tariff>> = tariffDao.getTariffsByMeterId(meterId)
    
    suspend fun getCurrentTariffByMeterId(meterId: Long, date: Long = System.currentTimeMillis()): Tariff? = 
        tariffDao.getCurrentTariffByMeterId(meterId, date)
    
    suspend fun getActiveTariffByMeterId(meterId: Long): Tariff? = tariffDao.getActiveTariffByMeterId(meterId)
    
    suspend fun insertTariff(tariff: Tariff): Long = tariffDao.insertTariff(tariff)
    
    suspend fun updateTariff(tariff: Tariff) = tariffDao.updateTariff(tariff)
    
    suspend fun deleteTariff(tariff: Tariff) = tariffDao.deleteTariff(tariff)
}