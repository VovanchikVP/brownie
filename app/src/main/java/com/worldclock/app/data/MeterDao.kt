package com.worldclock.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MeterDao {
    
    @Query("SELECT * FROM meters ORDER BY type, number")
    fun getAllMeters(): Flow<List<Meter>>
    
    @Query("SELECT * FROM meters WHERE id = :id")
    suspend fun getMeterById(id: Long): Meter?
    
    @Query("SELECT * FROM meters WHERE type = :type ORDER BY number")
    fun getMetersByType(type: MeterType): Flow<List<Meter>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeter(meter: Meter): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeters(meters: List<Meter>)
    
    @Update
    suspend fun updateMeter(meter: Meter)
    
    @Delete
    suspend fun deleteMeter(meter: Meter)
    
    @Query("DELETE FROM meters WHERE id = :id")
    suspend fun deleteMeterById(id: Long)
}