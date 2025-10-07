package com.worldclock.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TariffDao {
    
    @Query("SELECT * FROM tariffs ORDER BY startDate DESC")
    fun getAllTariffs(): Flow<List<Tariff>>
    
    @Query("SELECT * FROM tariffs WHERE meterId = :meterId ORDER BY startDate DESC")
    fun getTariffsByMeterId(meterId: Long): Flow<List<Tariff>>
    
    @Query("SELECT * FROM tariffs WHERE id = :id")
    suspend fun getTariffById(id: Long): Tariff?
    
    @Query("SELECT * FROM tariffs WHERE meterId = :meterId AND (:date BETWEEN startDate AND COALESCE(endDate, :date)) ORDER BY startDate DESC LIMIT 1")
    suspend fun getCurrentTariffByMeterId(meterId: Long, date: Long = System.currentTimeMillis()): Tariff?
    
    @Query("SELECT * FROM tariffs WHERE meterId = :meterId AND endDate IS NULL ORDER BY startDate DESC LIMIT 1")
    suspend fun getActiveTariffByMeterId(meterId: Long): Tariff?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTariff(tariff: Tariff): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTariffs(tariffs: List<Tariff>)
    
    @Update
    suspend fun updateTariff(tariff: Tariff)
    
    @Delete
    suspend fun deleteTariff(tariff: Tariff)
    
    @Query("DELETE FROM tariffs WHERE id = :id")
    suspend fun deleteTariffById(id: Long)
    
    @Query("DELETE FROM tariffs WHERE meterId = :meterId")
    suspend fun deleteTariffsByMeterId(meterId: Long)
}